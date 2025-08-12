package com.kcs3.auction.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendDto {

    private Long itemId;
    private String itemTitle;
    private String thumbnail;
    private Integer maxPrice;

    @Builder
    public RecommendDto(Long itemId, String itemTitle, String thumbnail, Integer maxPrice) {
        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.thumbnail = thumbnail;
        this.maxPrice = maxPrice;
    }
}