package com.kcs3.auction.entity;

import com.kcs3.auction.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Recommend")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = true)
public class Recommend extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendId", nullable = false)
    private Long recommendId;

    @Column(columnDefinition = "TEXT")
    private String embedding;

    @Column(columnDefinition = "TEXT")
    private String thEmbedding;

    @Column(columnDefinition = "TEXT")
    private String categoryEmbedding;

    @Column(columnDefinition = "TEXT")
    private String detailEmbedding;

    @Column(columnDefinition = "TEXT")
    private String representEmbedding;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Builder
    public Recommend(
            String embedding,
            String thEmbedding,
            String categoryEmbedding,
            String detailEmbedding,
            String representEmbedding,
            Long itemId
    ) {
        this.embedding = embedding;
        this.thEmbedding = thEmbedding;
        this.categoryEmbedding = categoryEmbedding;
        this.detailEmbedding = detailEmbedding;
        this.representEmbedding = representEmbedding;
        this.itemId = itemId;
    }
}

