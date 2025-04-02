package com.kcs3.auction.entity;

import com.kcs3.auction.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemQnaComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long itemQnaCommentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_qna_Id", nullable = false)
    private ItemQna itemQna;

    @Column(nullable = false)
    private String comment;

    @Builder
    public ItemQnaComment(ItemQna itemQna, String comment) {
        this.itemQna = itemQna;
        this.comment = comment;
    }
}
