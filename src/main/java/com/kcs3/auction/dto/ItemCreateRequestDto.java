package com.kcs3.auction.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemCreateRequestDto {

    @NotBlank
    private String title;

    @NotBlank
    private String contents;

    @NotNull
    private String category;

    @NotNull
    private Integer tradingMethod;

    @NotBlank
    private String region;

    @Positive
    private Integer startPrice;

    private Integer buyNowPrice;

    @NotNull
    @Future  // 마감일은 미래여야 함
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime finishTime;

    @Builder
    public ItemCreateRequestDto(
        String title, String contents,
        String category, Integer tradingMethod, String region,
        Integer startPrice, Integer buyNowPrice,
        LocalDateTime finishTime
    ) {
        this.title = title;
        this.contents = contents;
        this.category = category;
        this.tradingMethod = tradingMethod;
        this.region = region;
        this.startPrice = startPrice;
        this.buyNowPrice = buyNowPrice;
        this.finishTime = finishTime;
    }
}
