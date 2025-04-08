package com.kcs3.auction.repository;

import com.kcs3.auction.dto.AuctionInfoSummeryDto;
import com.kcs3.auction.entity.AuctionInfo;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionInfoRepository extends JpaRepository<AuctionInfo, Long> {

    // 사용자가 참여한 경매 물품 목록을 최근 입찰한 순서로 조회
    @Query("SELECT DISTINCT ai.item.itemId FROM AuctionInfo ai WHERE ai.userId = :userId ORDER BY ai.createdAt DESC")
    Slice<Long> findItemIdsByUserId(@Param("userId") Long userId, Pageable pageable);

    // 지역별 인기 물품 ID 목록 조회
    @Query("""
        SELECT i.itemId
        FROM AuctionInfo ai
        JOIN ai.item i
        JOIN i.region r
        WHERE i.isAuctionComplete = false AND r.regionId = :regionId
        GROUP BY i.itemId
        ORDER BY COUNT(ai.user.userId) DESC
        """)
    List<Long> findPopularProgressItemIdsByRegion(@Param("regionId") Long regionId, Pageable pageable);
}
