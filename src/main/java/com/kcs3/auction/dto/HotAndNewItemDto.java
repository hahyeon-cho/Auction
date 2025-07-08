package com.kcs3.auction.dto;

import com.kcs3.auction.entity.AuctionProgressItem;
import com.kcs3.auction.entity.Category;
import com.kcs3.auction.entity.Item;

public record HotAndNewItemDto(
    Long itemId,
    String itemTitle,
    String category,
    String thumbnail,
    Integer startPrice,
    Integer buyNowPrice

) {

    public static HotAndNewItemDto from(AuctionProgressItem progressItem) {
        Item item = progressItem.getItem();
        Category category = item.getCategory();

        return new HotAndNewItemDto(
            item.getItemId(),
            progressItem.getItemTitle(),
            category.getCategoryName(),
            progressItem.getThumbnail(),
            progressItem.getStartPrice(),
            progressItem.getBuyNowPrice()
        );
    }
}
