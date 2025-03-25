package com.kcs3.auction.entity;

import com.kcs3.auction.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "AuctionInfo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuctionInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auctionInfoId", nullable = false)
    private Long auctionInfoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itemId")
    private Item item;

    @Column(nullable = false)
    private int bidPrice;

    @Builder
    public AuctionInfo(User user, Item item, int bidPrice) {
        this.user = user;
        this.item = item;
        this.bidPrice = bidPrice;
    }
}
