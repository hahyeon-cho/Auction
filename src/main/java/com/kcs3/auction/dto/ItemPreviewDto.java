package com.kcs3.auction.dto;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemPreviewDto {

    private Long itemId;
    private String itemTitle;
    private String thumbnail;

    private String categoryName;
    private Integer tmCode;
    private String location;

    private Integer startPrice;
    private Integer buyNowPrice;
    private Integer maxPrice;
    private LocalDateTime bidFinishTime;

    private boolean isAuctionComplete;
    private Boolean isBidComplete;  // 거래 중 물품의 경우 null

    @Builder
    public ItemPreviewDto(
        Long itemId, String itemTitle, String thumbnail,
        String categoryName, Integer tmCode, String location,
        Integer startPrice, Integer buyNowPrice, Integer maxPrice,
        LocalDateTime bidFinishTime,
        boolean isAuctionComplete,
        Boolean isBidComplete
    ) {
        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.thumbnail = thumbnail;
        this.categoryName = categoryName;
        this.tmCode = tmCode;
        this.location = location;
        this.startPrice = startPrice;
        this.buyNowPrice = buyNowPrice;
        this.maxPrice = maxPrice;
        this.bidFinishTime = bidFinishTime;
        this.isAuctionComplete = isAuctionComplete;
        this.isBidComplete = isBidComplete;
    }
}
