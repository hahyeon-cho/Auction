package com.kcs3.auction.dto;

import lombok.Builder;

@Builder
public record AuctionPriceDto(int buyNowPrice,
                              int maxPrice
) {
}
