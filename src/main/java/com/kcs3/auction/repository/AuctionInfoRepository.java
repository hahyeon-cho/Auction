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

    // 물품 ID로 (입찰자 닉네임, 입찰가) 목록 조회
    @Query(
        "SELECT new com.kcs3.auction.dto.AuctionInfoSummeryDto(user.userNickname, ai.bidPrice) " +
            "FROM AuctionInfo ai " +
            "JOIN ai.user user " +
            "WHERE ai.item.itemId = :itemId")
    List<AuctionInfoSummeryDto> findInfoSummariesByItemId(Long itemId);

    // Hot 물품 10개 조회
    @Query("SELECT DISTINCT item.itemId " +
        "FROM AuctionInfo ai " +
        "JOIN ai.item item " +
        "WHERE item.isAuctionComplete = false " +
        "GROUP BY item.itemId " +
        "ORDER BY COUNT(ai.user) DESC")
    List<Long> findTop10ItemIds(Pageable pageable);

    // New 물품 10개 조회
    @Query("SELECT DISTINCT item.itemId " +
        "FROM Item item " + // 공백 추가
        "WHERE item.isAuctionComplete = false " +
        "ORDER BY item.itemId DESC")
    List<Long> findNew10ItemIds(Pageable pageable);
}
