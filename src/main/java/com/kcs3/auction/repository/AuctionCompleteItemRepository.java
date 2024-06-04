package com.kcs3.auction.repository;


import com.kcs3.auction.dto.AuctionPriceDto;
import com.kcs3.auction.entity.AuctionCompleteItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AuctionCompleteItemRepository extends JpaRepository<AuctionCompleteItem, Long> {
    @Query("SELECT new com.kcs3.auction.dto.AuctionPriceDto(aci.buyNowPrice, aci.maxPrice) " +
            "FROM AuctionCompleteItem aci " +
            "WHERE aci.item.itemId = :itemId")
    Optional<AuctionPriceDto> findPriceByItemItemId(Long itemId);

    Optional<AuctionCompleteItem> findByItemItemId(Long itemId);
}
