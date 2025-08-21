package com.kcs3.auction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.kcs3.auction.dto.ItemCreateRequestDto;
import com.kcs3.auction.entity.Category;
import com.kcs3.auction.entity.Region;
import com.kcs3.auction.entity.TradingMethod;
import com.kcs3.auction.entity.User;
import com.kcs3.auction.exception.CommonException;
import com.kcs3.auction.exception.ErrorCode;
import com.kcs3.auction.repository.AuctionProgressItemRepository;
import com.kcs3.auction.repository.CategoryRepository;
import com.kcs3.auction.repository.ItemElasticsearchRepository;
import com.kcs3.auction.repository.ItemRepository;
import com.kcs3.auction.repository.RegionRepository;
import com.kcs3.auction.repository.TradingMethodRepository;
import com.kcs3.auction.repository.UserRepository;
import com.kcs3.auction.support.TestFixture;
import com.kcs3.auction.utils.AuthUserProvider;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ServiceClientConfiguration;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.redis.host=localhost",
    "spring.redis.port=6379",

    "cloud.aws.credentials.accessKey=test-access-key",
    "cloud.aws.credentials.secretKey=test-secret-key",
    "cloud.aws.region.static=ap-northeast-2",
    "cloud.aws.s3.bucket=test-bucket",
    "cloud.aws.stack.auto=false",

    "spring.cloud.aws.parameterstore.enabled=false",
    "spring.cloud.aws.secretsmanager.enabled=false",
    "spring.cloud.aws.sqs.enabled=false",

    "spring.jwt.secret=test-jwt-secret-key-for-testing-purpose-only",
    "frontend.url=http://localhost:3000",
    "OPENAI_API_KEY=test-openai-api-key",
    "openai.api.key=test-openai-api-key"
})
@DisplayName("ItemService - 물품 등록 시 트랜젝션 롤백 테스트")
class ItemServiceRollbackTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuctionProgressItemRepository auctionProgressItemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TradingMethodRepository tradingMethodRepository;

    @Autowired
    private RegionRepository regionRepository;

    @MockitoBean private ItemElasticsearchRepository itemElasticsearchRepository;
    @MockitoBean private S3Client s3Client;
    @MockitoBean private RecommendationService itemRecommendationService;
    @MockitoBean private SearchEmbeddingService searchEmbeddingService;
    @MockitoBean private AuthUserProvider authUserProvider;

    private ItemCreateRequestDto requestDto;
    private List<MultipartFile> images;

    @BeforeEach
    void setUp() throws Exception {
        User seller = userRepository.save(
            TestFixture.user(null, "seller", "seller@test.com")
        );

        given(authUserProvider.getCurrentUser()).willReturn(seller);


        Category category = categoryRepository.save(TestFixture.category(null));
        TradingMethod tradingMethod = tradingMethodRepository.save(TestFixture.tradingMethod(null));
        Region region = regionRepository.save(TestFixture.region(null));

        requestDto = ItemCreateRequestDto.builder()
            .title("테스트 상품")
            .category(category.getCategoryName())
            .tradingMethod(tradingMethod.getTmCode())
            .region(region.getRegionName())
            .contents("내용")
            .startPrice(1000)
            .buyNowPrice(5000)
            .finishTime(LocalDateTime.now().plusDays(1))
            .build();


        MultipartFile mockImage = Mockito.mock(MultipartFile.class);
        given(mockImage.getOriginalFilename()).willReturn("test.jpg");
        given(mockImage.getContentType()).willReturn("image/jpeg");
        given(mockImage.getSize()).willReturn(1024L);
        given(mockImage.getInputStream())
            .willReturn(new ByteArrayInputStream("test".getBytes()));

        images = List.of(mockImage);

        given(searchEmbeddingService.createEmbedding(anyString()))
            .willReturn(new float[]{0.1f, 0.2f});

        S3ServiceClientConfiguration cfg = Mockito.mock(S3ServiceClientConfiguration.class);
        given(cfg.region()).willReturn(software.amazon.awssdk.regions.Region.AP_NORTHEAST_2);
        given(s3Client.serviceClientConfiguration()).willReturn(cfg);

        given(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .willReturn(PutObjectResponse.builder().build());
    }

    @Test
    @DisplayName("Elasticsearch 저장 실패 → JPA 트랜잭션 롤백 → DB에 엔티티가 남지 않음")
    void rollbackWhenElasticSaveFails() {
        // given
        given(itemElasticsearchRepository.save(any()))
            .willThrow(new RuntimeException("Elasticsearch 저장 실패"));

        // when / then
        assertThatThrownBy(() ->
            itemService.createAuctionItem(requestDto, images)
        ).isInstanceOf(CommonException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ITEM_ELASTIC_SAVE_FAILED);

        // then
        assertThat(itemRepository.count()).isZero();
        assertThat(auctionProgressItemRepository.count()).isZero();
    }
}