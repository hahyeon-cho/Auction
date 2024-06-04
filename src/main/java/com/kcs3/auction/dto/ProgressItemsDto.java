package com.kcs3.auction.dto;



import com.kcs3.auction.entity.AuctionCompleteItem;
import com.kcs3.auction.entity.AuctionProgressItem;
import lombok.Builder;

@Builder
public record ProgressItemsDto(
        Long itemId,
        String itemTitle,
        String category,
        int tradingMethod,
        String thumbnail,
        int startPrice,
        int currentPrice,
        Integer  buyNowPrice,
        boolean isAuctionComplete
) {
    public static ProgressItemsDto fromProgressEntity(AuctionProgressItem progressItem) {
        return ProgressItemsDto.builder()
                .itemId(progressItem.getItem().getItemId())
                .itemTitle(progressItem.getItemTitle())
                .category(progressItem.getItem().getCategory().getCategory())
                .tradingMethod(progressItem.getItem().getTradingMethod().getTradingMethod())
                .thumbnail(progressItem.getThumbnail())
                .startPrice(progressItem.getStartPrice())
                .currentPrice((progressItem.getMaxPersonNickName() != null)?progressItem.getMaxPrice():0)
                .buyNowPrice(progressItem.getBuyNowPrice())
                .isAuctionComplete(progressItem.getItem().isAuctionComplete())
                .build();
    }

    public static ProgressItemsDto fromCompletionEntity(AuctionCompleteItem completeItem) {
        return ProgressItemsDto.builder()
                .itemId(completeItem.getItem().getItemId())
                .itemTitle(completeItem.getItemTitle())
                .category(completeItem.getItem().getCategory().getCategory())
                .tradingMethod(completeItem.getItem().getTradingMethod().getTradingMethod())
                .thumbnail(completeItem.getThumbnail())
                .startPrice(completeItem.getStartPrice())
                .currentPrice((completeItem.getMaxPersonNickName() != null)?completeItem.getMaxPrice():0)
                .buyNowPrice(completeItem.getBuyNowPrice())
                .isAuctionComplete(completeItem.getItem().isAuctionComplete())
                .build();
    }



}
