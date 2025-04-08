package com.kcs3.auction.dto;

import com.kcs3.auction.entity.AuctionProgressItem;
import com.kcs3.auction.entity.Category;
import com.kcs3.auction.entity.Item;
import lombok.Builder;

@Builder
public record RedisItemDto(
    Long itemId,
    String itemTitle,
    String category,
    String thumbnail,
    Integer startPrice,
    Integer buyNowPrice

) {

    public static RedisItemDto from(AuctionProgressItem progressItem) {
        Item item = progressItem.getItem();
        Category category = item.getCategory();

        return RedisItemDto.builder()
            .itemId(item.getItemId())
            .itemTitle(progressItem.getItemTitle())
            .category(category.getCategoryName())
            .thumbnail(progressItem.getThumbnail())
            .startPrice(progressItem.getStartPrice())
            .buyNowPrice(progressItem.getBuyNowPrice())
            .build();
    }
}
