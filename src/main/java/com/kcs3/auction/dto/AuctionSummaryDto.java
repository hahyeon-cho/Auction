package com.kcs3.auction.dto;

import com.kcs3.auction.entity.AuctionCompleteItem;
import com.kcs3.auction.entity.AuctionProgressItem;
import java.time.LocalDateTime;

public record AuctionSummaryDto(
    String itemTitle,
    String location,
    Integer startPrice,
    Integer buyNowPrice,
    Integer maxPrice,
    LocalDateTime bidFinishTime
) {
    public static AuctionSummaryDto from(AuctionProgressItem item) {
        return new AuctionSummaryDto(
            item.getItemTitle(),
            item.getLocation(),
            item.getStartPrice(),
            item.getBuyNowPrice(),
            item.getMaxPrice(),
            item.getBidFinishTime()
        );
    }

    public static AuctionSummaryDto from(AuctionCompleteItem item) {
        return new AuctionSummaryDto(
            item.getItemTitle(),
            item.getLocation(),
            item.getStartPrice(),
            item.getBuyNowPrice(),
            item.getMaxPrice(),
            item.getBidFinishTime()
        );
    }
}
