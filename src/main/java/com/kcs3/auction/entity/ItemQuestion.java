package com.kcs3.auction.entity;

import com.kcs3.auction.model.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemQuestion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long itemQuestionId;

    // === Core Relationships ===
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_detail_id", nullable = false)
    private ItemDetail itemDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // === Basic column ===
    @Column(nullable = false)
    private String questionContent;

    // === Other Relationships ===
    // --- OneToMany ---
    @OneToMany(mappedBy = "itemQuestion", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ItemAnswer> answers = new ArrayList<>();

    @Builder
    public ItemQuestion(ItemDetail itemDetail, User user, String questionContent) {
        this.itemDetail = itemDetail;
        this.user = user;
        this.questionContent = questionContent;
    }

    // === Add & Update ===
    public void addAnswer(ItemAnswer answer) { answers.add(answer); }
}
