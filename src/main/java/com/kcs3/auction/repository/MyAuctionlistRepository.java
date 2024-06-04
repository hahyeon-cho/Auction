package com.kcs3.auction.repository;

import com.kcs3.auction.entity.AuctionInfo;
import com.kcs3.auction.entity.Item;
import com.kcs3.auction.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository

public interface MyAuctionlistRepository extends JpaRepository<AuctionInfo,Long> {
    @Query("SELECT DISTINCT item " +
            "FROM AuctionInfo ai " +
            "JOIN ai.item item " +
            "WHERE ai.user = :user " +
            "GROUP BY item.itemId ")
    Slice<Item> findByUser(@Param("user") User user, Pageable pageable);
}
