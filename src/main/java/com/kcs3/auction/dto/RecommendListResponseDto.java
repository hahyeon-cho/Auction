package com.kcs3.auction.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendListResponseDto {

    private List<RecommendDto> items;
    private Integer totalCount;

    @Builder
    public RecommendListResponseDto(List<RecommendDto> items, Integer totalCount) {
        this.items = items;
        this.totalCount = totalCount;
    }

    public static RecommendListResponseDto from(List<RecommendDto> items) {
        return RecommendListResponseDto.builder()
            .items(items)
            .totalCount(items.size())
            .build();
    }
}