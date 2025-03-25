package com.kcs3.auction.entity;

import com.kcs3.auction.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ItemQuestion")
@DynamicUpdate
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemQuestion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "itemQuestionId", nullable = false)
    private Long itemQuestionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itemDetailId", nullable = false)
    private ItemDetail itemDetailId;

    @Column(nullable = false)
    private Long questionUserId;

    @Column(nullable = false)
    private String questionContents;

    @OneToMany(mappedBy = "questionId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QnaComment> comments = new ArrayList<>();

    @Builder
    public ItemQuestion(ItemDetail itemDetailId, Long questionUserId, String questionContents) {
        this.itemDetailId = itemDetailId;
        this.questionUserId = questionUserId;
        this.questionContents = questionContents;
    }
}
