package com.kcs3.auction.repository;

import com.kcs3.auction.entity.AuctionCompleteItem;
import com.kcs3.auction.entity.Item;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AuctionCompleteItemRepository extends JpaRepository<AuctionCompleteItem, Long> {

    // 사용자 ID 기준으로 사용자가 낙찰한 물품 ID 목록 조회 (최신순)
    @Query("SELECT aci.item.itemId FROM AuctionCompleteItem aci WHERE aci.userId = :userId ORDER BY aci.createdAt DESC")
    Slice<Long> findItemIdsByUserId(Long userId, Pageable pageable);

    // 특정 물품에 관한 경매 완료 테이블 정보 조회
    Optional<AuctionCompleteItem> findCompleteItemByItem(Item item);

    // 물품 ID에 대한 경매 완료 물품 정보 조회
    Optional<AuctionCompleteItem> findByItemItemId(Long itemId);

}
