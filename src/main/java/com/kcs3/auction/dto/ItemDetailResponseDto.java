package com.kcs3.auction.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemDetailResponseDto {

    private Long itemId;
    private boolean isAuctionComplete;
    private LocalDateTime itemCreatedAt;

    private Long sellerId;
    private String nickname;

    private String categoryName;
    private Integer tmCode;
    private String location;

    private String itemTitle;
    private Integer startPrice;
    private Integer buyNowPrice;
    private Integer maxPrice;
    private LocalDateTime bidFinishTime;

    private String itemDetailContent;
    private List<ImageDto> images;
    private List<QuestionWithAnswerDto> questions;

    @Builder
    public ItemDetailResponseDto(
        Long itemId, boolean isAuctionComplete, LocalDateTime itemCreatedAt,
        Long sellerId, String nickname,
        String itemTitle,
        String categoryName, Integer tmCode, String location,
        Integer startPrice, Integer buyNowPrice, Integer maxPrice,
        LocalDateTime bidFinishTime,
        String itemDetailContent,
        List<ImageDto> images,
        List<QuestionWithAnswerDto> questions
    ) {
        this.itemId = itemId;
        this.isAuctionComplete = isAuctionComplete;
        this.itemCreatedAt = itemCreatedAt;
        this.sellerId = sellerId;
        this.nickname = nickname;
        this.itemTitle = itemTitle;
        this.categoryName = categoryName;
        this.tmCode = tmCode;
        this.location = location;
        this.startPrice = startPrice;
        this.buyNowPrice = buyNowPrice;
        this.maxPrice = maxPrice;
        this.bidFinishTime = bidFinishTime;
        this.itemDetailContent = itemDetailContent;
        this.images = images;
        this.questions = questions;
    }
}
