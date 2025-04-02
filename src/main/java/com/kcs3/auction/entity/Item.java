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
import jakarta.persistence.OneToOne;
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
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "trading_method_id", nullable = false)
    private TradingMethod tradingMethod;

    @ManyToOne
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @OneToOne(mappedBy = "item", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ItemDetail itemDetail;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ItemLike> itemLikes = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.REMOVE)
    private final List<AuctionInfo> auctionInfos = new ArrayList<>();

    @Column(nullable = false)
    private boolean isAuctionComplete;

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
        this.setItemDetail(itemDetail);
        this.isAuctionComplete = false;
    }

    // Setter
    public void setItemDetail(ItemDetail itemDetail) {
        this.itemDetail = itemDetail;
        if (itemDetail.getItem() != this) {
            itemDetail.setItem(this);
        }
    }

    // modifier
    public void addLike(ItemLike itemLike) { this.itemLikes.add(itemLike); }

    public void addAuctionInfo(AuctionInfo auctionInfo) { this.auctionInfos.add(auctionInfo); }

    public void endAuction() {
        this.isAuctionComplete = true;
    }
}
