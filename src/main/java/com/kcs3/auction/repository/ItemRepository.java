package com.kcs3.auction.repository;

import com.kcs3.auction.entity.AuctionCompleteItem;
import com.kcs3.auction.entity.AuctionProgressItem;
import com.kcs3.auction.entity.Item;
import com.kcs3.auction.entity.User;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // 최근 등록된 아이템 조회
    Item findTopByOrderByItemIdDesc();

    // 아이템 ID로 판매자 ID 조회
    @Query("SELECT i.seller.userId FROM Item i WHERE i.itemId = :itemId")
    Long findSellerIdByItemId(@Param("itemId") Long itemId);

    // Redis에 저장할 대상인 물품 ID로 경매 진행 중 물품 조회 (HOT/NEW 용도)
    @Query("SELECT api " +
        "FROM AuctionProgressItem api " +
        "JOIN FETCH api.item i " +
        "JOIN FETCH i.category c " +
        "WHERE (i.itemId = :itemId)")
    AuctionProgressItem findByHotItemList(@Param("itemId") Long itemId);

    // 경매 진행 중 물품 목록 조회
    @Query("SELECT ap FROM AuctionProgressItem ap " +
        "JOIN FETCH ap.item item " +
        "JOIN FETCH item.category category " +
        "JOIN FETCH item.tradingMethod method " +
        "JOIN FETCH item.region region " +
        "WHERE (:category IS NULL OR category.category = :category) " +
        "AND (:method IS NULL OR method.tradingMethod = :method OR method.tradingMethod = 3) " +
        "AND (:region IS NULL OR region.region = :region) " +
        "ORDER BY item.itemId DESC")
    Slice<AuctionProgressItem> findByProgressItemWithLocationAndMethodAndRegion(
        @Param("category") String category,
        @Param("method") Integer method,
        @Param("region") String region,
        Pageable pageable);

    // 경매 완료된 물품 목록 조회
    @Query("SELECT ac FROM AuctionCompleteItem ac " +
        "JOIN FETCH ac.item item " +
        "JOIN FETCH item.category category " +
        "JOIN FETCH item.tradingMethod method " +
        "JOIN FETCH item.region region " +
        "WHERE (:category IS NULL OR category.category = :category) " +
        "AND (:method IS NULL OR method.tradingMethod = :method OR method.tradingMethod = 3) " +
        "AND (:region IS NULL OR region.region = :region) " +
        "ORDER BY item.itemId DESC")
    Slice<AuctionCompleteItem> findByCompleteItemWithLocationAndMethodAndRegion(
        @Param("category") String category,
        @Param("method") Integer method,
        @Param("region") String region,
        Pageable pageable);

    // 경매 진행 중 물품 목록 조회 + itemIds 검색 키워드 기반
    @Query("SELECT ap FROM AuctionProgressItem ap " +
        "JOIN FETCH ap.item item " +
        "JOIN FETCH item.category category " +
        "JOIN FETCH item.tradingMethod method " +
        "JOIN FETCH item.region region " +
        "WHERE (:category IS NULL OR category.category = :category) " +
        "AND (:method IS NULL OR method.tradingMethod = :method OR method.tradingMethod = 3) " +
        "AND (:region IS NULL OR region.region = :region) " +
        "AND item.itemId IN :itemIds " +
        "ORDER BY item.itemId DESC")
    Slice<AuctionProgressItem> findByProgressItemWithLocationAndMethodAndRegionAndItemIds(
        @Param("category") String category,
        @Param("method") Integer method,
        @Param("region") String region,
        @Param("itemIds") List<Long> itemIds);


    Slice<Item> findBySeller(User user, Pageable pageable);
}
