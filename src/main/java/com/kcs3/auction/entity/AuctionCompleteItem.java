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
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuctionCompleteItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long auctionCompleteItemId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false, unique = true)
    private Item item;

    @Column(nullable = false)
    private String itemTitle;

    @Column(nullable = false)
    private String thumbnail;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Integer startPrice;

    private Integer buyNowPrice;

    @Column(nullable = false)
    private LocalDateTime bidFinishTime;

    // === max bid info ===
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "max_bid_user_id")
    private User maxBidUser;

    private String maxBidUserNickname;

    @Column(nullable = false)
    private Integer maxPrice;

    // === Additional column ===
    @Column(nullable = false)
    private boolean isBidComplete;

    @Builder
    public AuctionCompleteItem(
        Item item, String itemTitle, String thumbnail, String location,
        Integer startPrice, Integer buyNowPrice, LocalDateTime bidFinishTime,
        User maxBidUser, String maxBidUserNickname, Integer maxPrice,
        boolean isBidComplete
    ) {
        this.item = item;
        this.itemTitle = itemTitle;
        this.thumbnail = thumbnail;
        this.location = location;
        this.startPrice = startPrice;
        this.buyNowPrice = buyNowPrice;
        this.bidFinishTime = bidFinishTime;
        this.maxBidUser = maxBidUser;
        this.maxBidUserNickname = maxBidUserNickname;
        this.maxPrice = maxPrice;
        this.isBidComplete = isBidComplete;
    }
}
