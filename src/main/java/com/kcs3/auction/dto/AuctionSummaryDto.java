package com.kcs3.auction.dto;

import com.kcs3.auction.entity.AuctionCompleteItem;
import com.kcs3.auction.entity.AuctionProgressItem;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record AuctionSummaryDto(
    String itemTitle,
    String location,
    Integer startPrice,
    Integer buyNowPrice,
    Integer maxPrice,
    LocalDateTime bidFinishTime
) {
    public static AuctionSummaryDto from(AuctionProgressItem item) {
        return AuctionSummaryDto.builder()
            .itemTitle(item.getItemTitle())
            .location(item.getLocation())
            .startPrice(item.getStartPrice())
            .buyNowPrice(item.getBuyNowPrice())
            .maxPrice(item.getMaxPrice())
            .bidFinishTime(item.getBidFinishTime())
            .build();
    }

    public static AuctionSummaryDto from(AuctionCompleteItem item) {
        return AuctionSummaryDto.builder()
            .itemTitle(item.getItemTitle())
            .location(item.getLocation())
            .startPrice(item.getStartPrice())
            .buyNowPrice(item.getBuyNowPrice())
            .maxPrice(item.getMaxPrice())
            .bidFinishTime(item.getBidFinishTime())
            .build();
    }
}
