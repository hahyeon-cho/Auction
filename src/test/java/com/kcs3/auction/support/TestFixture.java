package com.kcs3.auction.support;

import com.kcs3.auction.entity.Category;
import com.kcs3.auction.entity.Item;
import com.kcs3.auction.entity.ItemDetail;
import com.kcs3.auction.entity.Region;
import com.kcs3.auction.entity.TradingMethod;
import com.kcs3.auction.entity.User;
import org.springframework.test.util.ReflectionTestUtils;

public final class TestFixture {

    private TestFixture() {
    }

    // ===== User =====
    public static User user(Long userId, String nickname, String email) {
        User user = User.builder()
            .nickname(nickname)
            .email(email)
            .userPoint(0)
            .build();

        ReflectionTestUtils.setField(user, "userId", userId);
        return user;
    }

    // ===== Item =====
    public static Item item(Long itemId, User seller) {
        Item item = Item.builder()
            .seller(seller)
            .category(category(1L))
            .tradingMethod(tradingMethod(1L))
            .region(region(1L))
            .itemDetail(itemDetail())
            .build();

        ReflectionTestUtils.setField(item, "itemId", itemId);
        ReflectionTestUtils.setField(item, "isAuctionComplete", false);
        return item;
    }

    public static ItemDetail itemDetail() {
        return ItemDetail.builder()
            .itemDetailContent("테스트 상품 상세")
            .build();
    }

    // ===== Item Option =====
    public static Category category(Long categoryId) {
        Category category = Category.builder()
            .categoryName("전자기기")
            .build();

        ReflectionTestUtils.setField(category, "categoryId", categoryId);
        return category;
    }

    public static TradingMethod tradingMethod(Long tradingMethodId) {
        TradingMethod tradingMethod = TradingMethod.builder()
            .tmCode(1)
            .build();

        ReflectionTestUtils.setField(tradingMethod, "tradingMethodId", tradingMethodId);
        return tradingMethod;
    }

    public static Region region(Long regionId) {
        Region region = Region.builder()
            .regionName("서울")
            .build();

        ReflectionTestUtils.setField(region, "regionId", regionId);
        return region;
    }
}