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
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long itemId;

    // === Core Relationships ===
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    // === Basic column ===
    @Column(nullable = false)
    private boolean isAuctionComplete;

    // === Other Relationships ===
    // --- ManyToOne ---
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trading_method_id", nullable = false)
    private TradingMethod tradingMethod;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    // --- OneToOne ---
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    @JoinColumn(name = "item_detail_id", nullable = false, unique = true)
    private ItemDetail itemDetail;

    @Builder
    public Item(
        User seller,
        Category category,
        TradingMethod tradingMethod,
        Region region,
        ItemDetail itemDetail
    ) {
        this.seller = seller;
        this.category = category;
        this.tradingMethod = tradingMethod;
        this.region = region;
        this.itemDetail = itemDetail;
        this.isAuctionComplete = false;
    }

    // === Add & Update ===
    public void updateAuctionStatus() {
        this.isAuctionComplete = true;
    }
}
