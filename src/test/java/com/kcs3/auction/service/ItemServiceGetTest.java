package com.kcs3.auction.service;

import static com.kcs3.auction.support.TestFixture.auctionCompleteItem;
import static com.kcs3.auction.support.TestFixture.auctionProgressItem;
import static com.kcs3.auction.support.TestFixture.category;
import static com.kcs3.auction.support.TestFixture.item;
import static com.kcs3.auction.support.TestFixture.itemDetail;
import static com.kcs3.auction.support.TestFixture.markAuctionComplete;
import static com.kcs3.auction.support.TestFixture.tradingMethod;
import static com.kcs3.auction.support.TestFixture.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import com.kcs3.auction.dto.ItemDetailResponseDto;
import com.kcs3.auction.entity.AuctionCompleteItem;
import com.kcs3.auction.entity.AuctionProgressItem;
import com.kcs3.auction.entity.Category;
import com.kcs3.auction.entity.Item;
import com.kcs3.auction.entity.ItemDetail;
import com.kcs3.auction.entity.ItemImage;
import com.kcs3.auction.entity.TradingMethod;
import com.kcs3.auction.entity.User;
import com.kcs3.auction.exception.CommonException;
import com.kcs3.auction.exception.ErrorCode;
import com.kcs3.auction.repository.AuctionCompleteItemRepository;
import com.kcs3.auction.repository.AuctionProgressItemRepository;
import com.kcs3.auction.repository.ItemRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemService - 물품 상세 조회")
class ItemServiceGetTest {

    @Mock private ItemRepository itemRepository;
    @Mock private AuctionProgressItemRepository auctionProgressItemRepository;
    @Mock private AuctionCompleteItemRepository auctionCompleteItemRepository;

    @InjectMocks
    private ItemService itemService;

    private User seller;
    private Category category;
    private TradingMethod tradingMethod;
    private Item item;
    private ItemDetail itemDetail;

    private static final Long USER_ID = 1L;
    private static final Long ITEM_ID = 100L;

    @BeforeEach
    void setUp() {
        seller = user(USER_ID, "seller", "seller@test.com");

        category = category(1L); // categoryName = "카테고리"
        tradingMethod = tradingMethod(1L); // tmCode = "AUCTION"

        item = item(ITEM_ID, seller);
        itemDetail = itemDetail(); // itemDetailContent 포함

        ItemImage image1 = ItemImage.builder()
            .itemDetail(itemDetail)
            .url("https://test.com/image1.jpg")
            .build();

        ItemImage image2 = ItemImage.builder()
            .itemDetail(itemDetail)
            .url("https://test.com/image2.jpg")
            .build();

        itemDetail.addImage(image1);
        itemDetail.addImage(image2);

        ReflectionTestUtils.setField(item, "itemDetail", itemDetail);
        ReflectionTestUtils.setField(item, "category", category);
        ReflectionTestUtils.setField(item, "tradingMethod", tradingMethod);
        ReflectionTestUtils.setField(item, "seller", seller);
    }

    @Test
    @DisplayName("경매 진행 중 물품 조회 성공")
    void loadItemDetailProgressSuccess() {
        // given
        AuctionProgressItem auctionProgressItem =
            auctionProgressItem(item, "테스트 상품", 10000, 15000);

        given(itemRepository.findById(ITEM_ID)).willReturn(Optional.of(item));
        given(auctionProgressItemRepository.findByItemItemId(ITEM_ID))
            .willReturn(Optional.of(auctionProgressItem));

        // when
        ItemDetailResponseDto result = itemService.loadItemDetail(ITEM_ID);

        // then - 기본 정보
        assertThat(result).isNotNull();
        assertThat(result.getItemId()).isEqualTo(ITEM_ID);
        assertThat(result.isAuctionComplete()).isFalse();

        // then - 경매 정보
        assertThat(result.getItemTitle()).isEqualTo("테스트 상품");
        assertThat(result.getStartPrice()).isEqualTo(10000);
        assertThat(result.getMaxPrice()).isEqualTo(15000);
        assertThat(result.getBuyNowPrice()).isEqualTo(50000);
        assertThat(result.getBidFinishTime()).isNotNull();
        assertThat(result.getLocation()).isNotBlank();

        // then - 판매자 정보
        assertThat(result.getSellerId()).isEqualTo(USER_ID);
        assertThat(result.getNickname()).isEqualTo("seller");

        // then - 카테고리 / 거래 방식
        assertThat(result.getCategoryName())
            .isEqualTo(category.getCategoryName());
        assertThat(result.getTmCode())
            .isEqualTo(tradingMethod.getTmCode());

        // then - 상세 설명
        assertThat(result.getItemDetailContent())
            .isEqualTo(itemDetail.getItemDetailContent());

        // then - 이미지
        assertThat(result.getImages()).hasSize(2);

        // then - 문의 (비어 있어도 매핑 여부 검증)
        assertThat(result.getQuestions()).isNotNull();
    }

    @Test
    @DisplayName("경매 완료 물품 조회 성공")
    void loadItemDetailCompleteSuccess() {
        // given
        markAuctionComplete(item);

        AuctionCompleteItem auctionCompleteItem =
            auctionCompleteItem(item, "완료된 상품", 45000);

        given(itemRepository.findById(ITEM_ID)).willReturn(Optional.of(item));
        given(auctionCompleteItemRepository.findByItemItemId(ITEM_ID))
            .willReturn(Optional.of(auctionCompleteItem));

        // when
        ItemDetailResponseDto result = itemService.loadItemDetail(ITEM_ID);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getItemId()).isEqualTo(ITEM_ID);
        assertThat(result.isAuctionComplete()).isTrue();
        assertThat(result.getItemTitle()).isEqualTo("완료된 상품");
        assertThat(result.getMaxPrice()).isEqualTo(45000);

        // 단순 매핑 필드 검증
        assertThat(result.getCategoryName())
            .isEqualTo(category.getCategoryName());
        assertThat(result.getTmCode())
            .isEqualTo(tradingMethod.getTmCode());
    }

    @Test
    @DisplayName("물품 없음 → 조회 실패")
    void loadItemDetailItemNotFound() {
        given(itemRepository.findById(anyLong()))
            .willReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.loadItemDetail(ITEM_ID))
            .isInstanceOf(CommonException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ITEM_NOT_FOUND);
    }

    @Test
    @DisplayName("경매 진행 정보 없음 → 조회 실패")
    void loadItemDetailProgressNotFound() {
        given(itemRepository.findById(ITEM_ID))
            .willReturn(Optional.of(item));
        given(auctionProgressItemRepository.findByItemItemId(ITEM_ID))
            .willReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.loadItemDetail(ITEM_ID))
            .isInstanceOf(CommonException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ITEM_NOT_FOUND);
    }

    @Test
    @DisplayName("경매 완료 정보 없음 → 조회 실패")
    void loadItemDetailCompleteNotFound() {
        markAuctionComplete(item);

        given(itemRepository.findById(ITEM_ID))
            .willReturn(Optional.of(item));
        given(auctionCompleteItemRepository.findByItemItemId(ITEM_ID))
            .willReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.loadItemDetail(ITEM_ID))
            .isInstanceOf(CommonException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ITEM_NOT_FOUND);
    }
}
