package com.kcs3.auction.repository;

import com.kcs3.auction.entity.AuctionProgressItem;
import com.kcs3.auction.entity.Item;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuctionProgressItemRepository extends JpaRepository<AuctionProgressItem, Long> {

    // 특정 물품에 관한 경매 진행 테이블 정보 조회
    Optional<AuctionProgressItem> findProgressItemByItem(Item item);

    // 물품 ID로 경매 진행 중인 물품 정보 조회
    Optional<AuctionProgressItem> findByItemItemId(Long itemId);

    // 물품 ID 리스트로 경매 진행 중인 물품 리스트 정보 조회(물품 기본 정보 및 카테고리 정보 JOIN FETCH)
    @Query("""
        SELECT api FROM AuctionProgressItem api
        JOIN FETCH api.item i
        JOIN FETCH i.category
        WHERE api.item.itemId IN :itemIds
        """)
    List<AuctionProgressItem> findAllWithItemAndCategory(@Param("itemIds") List<Long> itemIds);

    // 현재 시간 기준으로 종료 처리되어야 할 경매 진행 물품 전체 조회
    Optional<List<AuctionProgressItem>> findAllByBidFinishTimeBefore(LocalDateTime now);
}

