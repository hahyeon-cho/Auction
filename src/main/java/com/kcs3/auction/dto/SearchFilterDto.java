package com.kcs3.auction.dto;

public record SearchFilterDto(
    Long categoryId,
    Long regionId,
    Long tradingMethodId,
    Boolean isAuctionComplete
) {
    public static SearchFilterDto of(
        Long categoryId,
        Long regionId,
        Long tradingMethodId,
        Boolean isAuctionComplete
    ) {
        return new SearchFilterDto(categoryId, regionId, tradingMethodId, isAuctionComplete);
    }
}
