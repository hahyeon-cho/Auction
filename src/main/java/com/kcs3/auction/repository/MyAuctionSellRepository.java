package com.kcs3.auction.repository;

import com.kcs3.auction.entity.Item;
import com.kcs3.auction.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyAuctionSellRepository extends JpaRepository<Item,Long> {
    //
    Slice<Item> findBySeller(User user, Pageable pageable);

    Optional<Item> findByItemId(Long itemId);
}
