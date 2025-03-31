package com.kcs3.auction.repository;

import com.kcs3.auction.dto.AuctionBidHighestDto;
import com.kcs3.auction.dto.AuctionPriceDto;
import com.kcs3.auction.entity.AuctionProgressItem;
import com.kcs3.auction.entity.Item;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuctionProgressItemRepository extends JpaRepository<AuctionProgressItem, Long> {

    // 물품 ID로 경매 진행 중인 물품 정보 조회
    Optional<AuctionProgressItem> findByItemItemId(Long itemId);

    // 현재 시간 기준으로 종료 처리되어야 할 경매 진행 물품 전체 조회
    Optional<List<AuctionProgressItem>> findAllByBidFinishTimeBefore(LocalDateTime now);

    // 물품 ID로 (즉시 구매가, 현재 최고 입찰가) 조회
    @Query("SELECT new com.kcs3.auction.dto.AuctionPriceDto(api.buyNowPrice, api.maxPrice) " +
        "FROM AuctionProgressItem api " +
        "WHERE api.item.itemId = :itemId")
    Optional<AuctionPriceDto> findPriceByItemItemId(Long itemId);

    // 경매 진행 물품 ID로 (최고 입찰자 ID, 닉네임, 입찰가) 조회
    @Query("SELECT new com.kcs3.auction.dto.AuctionBidHighestDto(" +
        "api.auctionProgressItemId, user.userId, user.userNickname, api.maxPrice) " +
        "FROM AuctionProgressItem api " +
        "LEFT JOIN api.user user " +
        "WHERE api.auctionProgressItemId = :auctionProgressItemId")
    Optional<AuctionBidHighestDto> findHighestBidByAuctionProgressItemId(
        @Param("auctionProgressItemId") Long auctionProgressItemId);

    Optional<List<AuctionProgressItem>> findAllByBidFinishTimeBefore(LocalDateTime now);
    AuctionProgressItem findAuctionProgressItemByItem(Item item);
}

