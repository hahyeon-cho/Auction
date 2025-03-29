package com.kcs3.auction.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemEmbeddingRequestDto {

    private Long itemId;
    private String title;
    private String thumbnailUrl;
    private String categoryName;
    private String description;

    @Builder
    public ItemEmbeddingRequestDto(
        Long itemId,
        String title,
        String thumbnailUrl,
        String categoryName,
        String description
    ) {
        this.itemId = itemId;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.categoryName = categoryName;
        this.description = description;
    }
}
