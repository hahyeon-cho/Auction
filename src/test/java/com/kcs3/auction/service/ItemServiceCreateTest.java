package com.kcs3.auction.service;

import static com.kcs3.auction.support.TestFixture.category;
import static com.kcs3.auction.support.TestFixture.createItemCreateRequestDto;
import static com.kcs3.auction.support.TestFixture.region;
import static com.kcs3.auction.support.TestFixture.tradingMethod;
import static com.kcs3.auction.support.TestFixture.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kcs3.auction.document.ItemDocument;
import com.kcs3.auction.dto.ItemCreateRequestDto;
import com.kcs3.auction.entity.AuctionProgressItem;
import com.kcs3.auction.entity.Item;
import com.kcs3.auction.entity.User;
import com.kcs3.auction.exception.CommonException;
import com.kcs3.auction.exception.ErrorCode;
import com.kcs3.auction.repository.AuctionProgressItemRepository;
import com.kcs3.auction.repository.CategoryRepository;
import com.kcs3.auction.repository.ItemElasticsearchRepository;
import com.kcs3.auction.repository.ItemRepository;
import com.kcs3.auction.repository.RegionRepository;
import com.kcs3.auction.repository.TradingMethodRepository;
import com.kcs3.auction.utils.AuthUserProvider;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ServiceClientConfiguration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemService - 물품 등록")
class ItemServiceCreateTest {

    @Mock private S3Client s3Client;
    @Mock private AuthUserProvider authUserProvider;
    @Mock private ItemRepository itemRepository;
    @Mock private AuctionProgressItemRepository auctionProgressItemRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private TradingMethodRepository tradingMethodRepository;
    @Mock private RegionRepository regionRepository;
    @Mock private SearchEmbeddingService searchEmbeddingService;
    @Mock private ItemElasticsearchRepository itemElasticsearchRepository;
    @Mock private RecommendationService itemRecommendationService;

    @InjectMocks
    private ItemService itemService;

    private User seller;
    private ItemCreateRequestDto requestDto;

    private static final Long USER_ID = 1L;
    private static final String S3_BUCKET = "test-bucket";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(itemService, "s3Bucket", S3_BUCKET);

        seller = user(USER_ID, "seller", "seller@test.com");
        requestDto = createItemCreateRequestDto();

        given(authUserProvider.getCurrentUser()).willReturn(seller);
    }

    @Test
    @DisplayName("물품 등록 성공")
    void createAuctionItemSuccess() throws Exception {
        // given
        MultipartFile image = createImage("test.jpg");
        List<MultipartFile> images = List.of(image);

        givenValidItemRegistrationContext();
        givenS3UploadSucceeds();
        given(searchEmbeddingService.createEmbedding(anyString())).willReturn(new float[]{0.1f, 0.2f});

        ArgumentCaptor<AuctionProgressItem> auctionCaptor =
            ArgumentCaptor.forClass(AuctionProgressItem.class);

        // when
        itemService.createAuctionItem(requestDto, images);

        // then
        then(auctionProgressItemRepository).should().save(auctionCaptor.capture());
        AuctionProgressItem savedAuction = auctionCaptor.getValue();
        assertThat(savedAuction.getMaxPrice()).isEqualTo(requestDto.getStartPrice());
        then(itemRepository).should().save(any(Item.class));
        then(itemElasticsearchRepository).should().save(any(ItemDocument.class));
    }

    @Test
    @DisplayName("여러 이미지 업로드 → 모든 이미지가 S3에 업로드됨")
    void createAuctionItemWithMultipleImages() throws Exception {
        // given
        List<MultipartFile> images = List.of(
            createImage("test1.jpg"),
            createImage("test2.jpg"),
            createImage("test3.jpg")
        );

        givenValidItemRegistrationContext();
        givenS3UploadSucceeds();
        given(searchEmbeddingService.createEmbedding(anyString())).willReturn(new float[]{0.1f, 0.2f});

        // when
        itemService.createAuctionItem(requestDto, images);

        // then
        then(s3Client).should(times(images.size())).putObject(
            any(PutObjectRequest.class),
            any(RequestBody.class)
        );
    }

    @Test
    @DisplayName("카테고리 없음 → 물품 등록 실패")
    void createAuctionItemCategoryNotFound() {
        // given
        MultipartFile image = createImage("test.jpg");
        List<MultipartFile> images = List.of(image);

        given(categoryRepository.findByCategoryName("전자기기")).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> itemService.createAuctionItem(requestDto, images))
            .isInstanceOf(CommonException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_NOT_FOUND);

        then(itemRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("거래 방법 없음 → 물품 등록 실패")
    void createAuctionItemTradingMethodNotFound() {
        // given
        MultipartFile image = createImage("test.jpg");
        List<MultipartFile> images = List.of(image);

        given(categoryRepository.findByCategoryName("전자기기")).willReturn(Optional.of(category(1L)));
        given(tradingMethodRepository.findByTmCode(1)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> itemService.createAuctionItem(requestDto, images))
            .isInstanceOf(CommonException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TRADING_METHOD_NOT_FOUND);

        then(itemRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("지역 없음 → 기타 지역으로 등록")
    void createAuctionItemRegionNotFoundUseDefault() throws Exception {
        // given
        MultipartFile image = createImage("test.jpg");
        List<MultipartFile> images = List.of(image);

        given(categoryRepository.findByCategoryName("전자기기")).willReturn(Optional.of(category(1L)));
        given(tradingMethodRepository.findByTmCode(1)).willReturn(Optional.of(tradingMethod(1L)));
        given(regionRepository.findByRegionName("알수없음")).willReturn(Optional.empty());
        given(regionRepository.findByRegionName("기타")).willReturn(Optional.of(region(99L, "기타")));
        givenS3UploadSucceeds();
        given(searchEmbeddingService.createEmbedding(anyString())).willReturn(new float[]{0.1f, 0.2f});

        ItemCreateRequestDto unknownRegionDto = createItemCreateRequestDto("알수없음");

        // when
        itemService.createAuctionItem(unknownRegionDto, images);

        // then
        then(itemRepository).should().save(any(Item.class));
        then(regionRepository).should().findByRegionName("기타");
    }

    @Test
    @DisplayName("지역(기타) 없음 → 물품 등록 실패")
    void createAuctionItemDefaultRegionNotFound() {
        // given
        MultipartFile image = createImage("test.jpg");
        List<MultipartFile> images = List.of(image);

        given(categoryRepository.findByCategoryName("전자기기")).willReturn(Optional.of(category(1L)));
        given(tradingMethodRepository.findByTmCode(1)).willReturn(Optional.of(tradingMethod(1L)));
        given(regionRepository.findByRegionName("알수없음")).willReturn(Optional.empty());
        given(regionRepository.findByRegionName("기타")).willReturn(Optional.empty());

        ItemCreateRequestDto unknownRegionDto = createItemCreateRequestDto("알수없음");

        // when / then
        assertThatThrownBy(() -> itemService.createAuctionItem(unknownRegionDto, images))
            .isInstanceOf(CommonException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DEFAULT_REGION_NOT_FOUND);
    }

    @Test
    @DisplayName("이미지 업로드 실패 → 물품 등록 실패")
    void createAuctionItemFileUploadFailed() {
        // given
        MultipartFile image = createImage("test.jpg");
        List<MultipartFile> images = List.of(image);

        givenValidItemRegistrationContext();
        given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .willThrow(new RuntimeException("S3 업로드 실패"));

        // when / then
        assertThatThrownBy(() -> itemService.createAuctionItem(requestDto, images))
            .isInstanceOf(CommonException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_UPLOAD_FAILED);

        then(itemRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("검색용 임베딩 생성 실패 → 예외 발생")
    void createAuctionItemSearchEmbeddingFailed() throws Exception {
        // given
        MultipartFile image = createImage("test.jpg");
        List<MultipartFile> images = List.of(image);

        givenValidItemRegistrationContext();
        givenS3UploadSucceeds();
        given(searchEmbeddingService.createEmbedding(anyString()))
            .willThrow(new RuntimeException("임베딩값 생성 실패"));

        // when / then
        assertThatThrownBy(() -> itemService.createAuctionItem(requestDto, images))
            .isInstanceOf(CommonException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SEARCH_EMBEDDING_FAILED);
    }

    @Test
    @DisplayName("Elasticsearch 저장 실패 → 물품 등록 실패")
    void createAuctionItemElasticSearchFailed() throws Exception {
        // given
        MultipartFile image = createImage("test.jpg");
        List<MultipartFile> images = List.of(image);

        givenValidItemRegistrationContext();
        givenS3UploadSucceeds();
        given(searchEmbeddingService.createEmbedding(anyString())).willReturn(new float[]{0.1f, 0.2f});
        given(itemElasticsearchRepository.save(any(ItemDocument.class)))
            .willThrow(new RuntimeException("Elasticsearch 저장 실패"));

        // when / then
        assertThatThrownBy(() -> itemService.createAuctionItem(requestDto, images))
            .isInstanceOf(CommonException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ITEM_ELASTIC_SAVE_FAILED);
    }

    @Test
    @DisplayName("추천용 임베딩 JSON 변환 실패 → 물품 등록 실패")
    void createAuctionItemRecommendEmbeddingFailed() throws Exception {
        // given
        MultipartFile image = createImage("test.jpg");
        List<MultipartFile> images = List.of(image);

        givenValidItemRegistrationContext();
        givenS3UploadSucceeds();
        given(searchEmbeddingService.createEmbedding(anyString())).willReturn(new float[]{0.1f, 0.2f});
        willThrow(new JsonProcessingException("JSON 변환 실패") {})
            .given(itemRecommendationService)
            .storeEmbeddingAfterItemRegister(
                isNull(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
            );

        // when / then
        assertThatThrownBy(() -> itemService.createAuctionItem(requestDto, images))
            .isInstanceOf(CommonException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RECOMMEND_EMBEDDING_SAVE_FAILED);
    }

    @Test
    @DisplayName("추천용 임베딩 생성 시 일반 예외 → 경고 로그만 출력하고 성공")
    void createAuctionItemRecommendEmbeddingGeneralException() throws Exception {
        // given
        MultipartFile image = createImage("test.jpg");
        List<MultipartFile> images = List.of(image);

        givenValidItemRegistrationContext();
        givenS3UploadSucceeds();
        given(searchEmbeddingService.createEmbedding(anyString())).willReturn(new float[]{0.1f, 0.2f});
        willThrow(new RuntimeException("일반 예외"))
            .given(itemRecommendationService)
            .storeEmbeddingAfterItemRegister(
                anyLong(),
                anyString(),
                anyString(),
                anyString(),
                anyString()
            );

        // when
        itemService.createAuctionItem(requestDto, images);

        // then
        then(itemRepository).should().save(any(Item.class));
        then(auctionProgressItemRepository).should().save(any(AuctionProgressItem.class));
    }


    // ===== 헬퍼 메서드 =====

    private void givenValidItemRegistrationContext() {
        given(categoryRepository.findByCategoryName("전자기기")).willReturn(Optional.of(category(1L)));
        given(tradingMethodRepository.findByTmCode(1)).willReturn(Optional.of(tradingMethod(1L)));
        given(regionRepository.findByRegionName("서울")).willReturn(Optional.of(region(1L)));
    }

    private MockMultipartFile createImage(String filename) {
        return new MockMultipartFile(
            "file",
            filename,
            "image/jpeg",
            "test".getBytes()
        );
    }

    private void givenS3UploadSucceeds() {
        S3ServiceClientConfiguration cfg = mock(S3ServiceClientConfiguration.class);
        given(cfg.region()).willReturn(Region.AP_NORTHEAST_2);
        given(s3Client.serviceClientConfiguration()).willReturn(cfg);
        given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .willReturn(PutObjectResponse.builder().build());
    }
}