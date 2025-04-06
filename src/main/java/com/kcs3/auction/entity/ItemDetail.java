package com.kcs3.auction.entity;

import com.kcs3.auction.model.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class ItemDetail extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long itemDetailId;

    @Column(nullable = false)
    private String itemDetailContent;

    // --- OneToMany ---
    @OneToMany(mappedBy = "itemDetail", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ItemImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "itemDetail", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ItemQuestion> qnas = new ArrayList<>();

    @Builder
    public ItemDetail(String itemDetailContent) {
        this.itemDetailContent = itemDetailContent;
    }

    // === Add & Update ===
    public void addImage(ItemImage img) { images.add(img); }

    public void addQna(ItemQuestion qna) { qnas.add(qna); }
}
