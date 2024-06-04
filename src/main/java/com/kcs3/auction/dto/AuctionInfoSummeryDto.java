package com.kcs3.auction.dto;

import lombok.Builder;

@Builder
public record AuctionInfoSummeryDto(String name,
                                    int price
) {
}
