package com.kcs3.auction.repository;


import com.kcs3.auction.entity.AuctionCompleteItem;
import com.kcs3.auction.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyCompleteItemRepository extends JpaRepository<AuctionCompleteItem,Long> {
    Slice<AuctionCompleteItem> findByUser(User user, Pageable pageable);
}
