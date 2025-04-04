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
import jakarta.persistence.Table;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
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
    @JoinColumn(name = "user_id")
    private User user;

    private String maxPersonNickName;

    @Column(nullable = false)
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
