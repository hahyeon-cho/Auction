package com.kcs3.auction.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemEmbeddingResponseDto {

    private String titleEmbedding;        // 제목 기반 임베딩
    private String thumbnailEmbedding;    // 썸네일 이미지 기반 임베딩
    private String categoryEmbedding;     // 카테고리 기반 임베딩
    private String descriptionEmbedding;  // 상세 설명 기반 임베딩

    @Builder
    public ItemEmbeddingResponseDto(
        String titleEmbedding,
        String thumbnailEmbedding,
        String categoryEmbedding,
        String descriptionEmbedding
    ) {
        this.titleEmbedding = titleEmbedding;
        this.thumbnailEmbedding = thumbnailEmbedding;
        this.categoryEmbedding = categoryEmbedding;
        this.descriptionEmbedding = descriptionEmbedding;
    }
}
