package com.kcs3.auction.repository;


import com.kcs3.auction.entity.AuctionCompleteItem;
import com.kcs3.auction.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyAuctionCompleteRepository extends JpaRepository<AuctionCompleteItem,Long> {
    //ItemId -> AuctionCompleteItem
    AuctionCompleteItem findCompleteItemByItem(Item item);

}
