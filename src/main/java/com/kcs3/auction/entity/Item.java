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
import jakarta.persistence.Table;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "Item")
@DynamicUpdate
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "itemId", nullable = false)
    private Long itemId;

    @OneToMany(mappedBy = "item", cascade = CascadeType.REMOVE)
    private List<LikeItem> likeItems;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sellerId")
    private User seller;

    @ManyToOne
    @JoinColumn(name = "categoryId", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "tradingMethodId", nullable = false)
    private TradingMethod tradingMethod;

    @ManyToOne
    @JoinColumn(name = "regionId", nullable = false)
    private Region region;

    @OneToOne(mappedBy = "item", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private ItemDetail itemDetail;

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

    public void setItemDetail(ItemDetail itemDetail) {
        this.itemDetail = itemDetail;
        if (itemDetail.getItem() != this) {
            itemDetail.setItem(this);
        }
    }

    public void endAuction() {
        this.isAuctionComplete = true;
    }
}
