package com.kcs3.auction.entity;


import com.kcs3.auction.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "AuctionCompleteItem")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuctionCompleteItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auctionCompleteItemId", nullable = false)
    private Long auctionCompleteItemId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itemId", nullable = false)
    private Item item;

    @Column(nullable = false)
    private String itemTitle;

    @Column(nullable = false)
    private String thumbnail;

    @Column(nullable = false)
    private int startPrice;

    private Integer buyNowPrice;

    @Column(nullable = false)
    private LocalDateTime bidFinishTime;

    @Column(nullable = false)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    private String maxPersonNickName;
    private Integer maxPrice;

    @Column(nullable = false)
    private boolean isBidComplete;

    @Builder
    public AuctionCompleteItem(
            Item item, String itemTitle, String thumbnail,
            int startPrice, Integer buyNowPrice, LocalDateTime bidFinishTime,
            String location, User user,
            String maxPersonNickName, Integer maxPrice, boolean isBidComplete
    ) {
        this.item = item;
        this.itemTitle = itemTitle;
        this.thumbnail = thumbnail;
        this.startPrice = startPrice;
        this.buyNowPrice = buyNowPrice;
        this.bidFinishTime = bidFinishTime;
        this.location = location;
        this.user = user;
        this.maxPersonNickName = maxPersonNickName;
        this.maxPrice = maxPrice;
        this.isBidComplete = isBidComplete;
    }
}
