package com.kcs3.auction.entity;

import com.kcs3.auction.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = true)
public class Recommend extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long recommendId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false, unique = true)
    private Item item;

    @Column(columnDefinition = "TEXT")
    private String titleEmbeddingVec;

    @Column(columnDefinition = "TEXT")
    private String thumbnailEmbeddingVec;

    @Column(columnDefinition = "TEXT")
    private String categoryEmbeddingVec;

    @Column(columnDefinition = "TEXT")
    private String detailEmbeddingVec;

    @Column(columnDefinition = "TEXT")
    private String representEmbeddingVec;

    @Builder
    public Recommend(
        Item item,
        String titleEmbeddingVec,
        String thumbnailEmbeddingVec,
        String categoryEmbeddingVec,
        String detailEmbeddingVec,
        String representEmbeddingVec
    ) {
        this.item = item;
        this.titleEmbeddingVec = titleEmbeddingVec;
        this.thumbnailEmbeddingVec = thumbnailEmbeddingVec;
        this.categoryEmbeddingVec = categoryEmbeddingVec;
        this.detailEmbeddingVec = detailEmbeddingVec;
        this.representEmbeddingVec = representEmbeddingVec;
    }

    // === Add & Update ===
    public void updateRepresentEmbeddingVec(String representingVecRepresent) {
        this.representEmbeddingVec = representingVecRepresent;
    }
}
