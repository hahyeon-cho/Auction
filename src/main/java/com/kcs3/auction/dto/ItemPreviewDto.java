package com.kcs3.auction.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemPreviewDto {

    private Long itemId;
    private String itemTitle;
    private String thumbnail;
    private String categoryName;
    private Integer tmCode;
    private String location;
    private Integer startPrice;
    private Integer maxPrice;
    private boolean isAuctionComplete;
    private boolean isBidComplete;  // 거래 중 물품의 경우 null
}