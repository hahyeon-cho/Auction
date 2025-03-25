package com.kcs3.auction.entity;

import com.kcs3.auction.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;

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

    @OneToMany(mappedBy = "item", cascade = CascadeType.REMOVE) // 찜 삭제 설정
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

    @Column(nullable = false)
    private boolean isAuctionComplete;

    @Builder
    public Item(
            User seller,
            Category category,
            TradingMethod tradingMethod,
            Region region,
            boolean isAuctionComplete
    ) {
        this.seller = seller;
        this.category = category;
        this.tradingMethod = tradingMethod;
        this.region = region;
        this.isAuctionComplete = isAuctionComplete;
    }

    public void endAuction() {
        this.isAuctionComplete = true;
    }
}
