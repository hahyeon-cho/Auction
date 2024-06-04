package com.kcs3.auction.repository;


import com.kcs3.auction.entity.AuctionProgressItem;
import com.kcs3.auction.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyAuctionProgressRepository extends JpaRepository<AuctionProgressItem,Long> {
    //ItemId -> AuctionProgressItem
    AuctionProgressItem findAuctionProgressItemByItem(Item item);
}
