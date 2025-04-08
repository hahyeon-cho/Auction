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

    // 필터 조건(카테고리, 거래방식, 지역, 경매상태 등)으로 경매 아이템 목록 조회
    // 경매 상태에 따라 경매 중 테이블/경매 완료 테이블 선택 조인
    // [TODO] 쿼리 재사용성을 위해 공통 조회 구조 유지 중. 성능이슈 또는 조건 복잡도 증가 시 분리 또는 QueryDSL 리팩토링 고려
    @Query(value = """
            SELECT new com.example.dto.ItemPreviewDto(
                i.id,
                COALESCE(api.itemTitle, aci.itemTitle),
                COALESCE(api.thumbnail, aci.thumbnail),
                c.categoryName,
                tm.tmCode,
                COALESCE(api.location, aci.location),
                COALESCE(api.startPrice, aci.startPrice),
                COALESCE(api.buyNowPirce, aci.buyNowPirce),
                COALESCE(api.maxPrice, aci.maxPrice),
                COALESCE(api.bidFinishTime, aci.bidFinishTime),
                i.isAuctionComplete,
                aci.isBidComplete
            )
            FROM Item i
            JOIN i.category c
            JOIN i.tradeMethod tm
            LEFT JOIN AuctionProgressItem api ON i.isAuctionComplete = false AND api.item = i
            LEFT JOIN AuctionCompleteItem aci ON i.isAuctionComplete = true AND aci.item = i
            WHERE (:itemIdList IS NULL OR i.itemId IN :itemIdList)
              AND (:sellerId IS NULL OR i.seller.id = :sellerId)
              AND (:categoryId IS NULL OR i.category.id = :categoryId)
              AND (:tradingMethodId IS NULL OR i.tradeMethod.id = :tradingMethodId)
              AND (:regionId IS NULL OR i.region.regionId = :regionId)
              AND (:isAuctionComplete IS NULL OR i.isAuctionComplete = :isAuctionComplete)
            ORDER BY i.createdAt DESC
        """, countQuery = """
            SELECT COUNT(i)
            FROM Item i
            WHERE (:itemIdList IS NULL OR i.itemId IN :itemIdList)
              AND (:sellerId IS NULL OR i.seller.id = :sellerId)
              AND (:categoryId IS NULL OR i.category.id = :categoryId)
              AND (:tradingMethodId IS NULL OR i.tradeMethod.id = :tradingMethodId)
              AND (:regionId IS NULL OR i.region.regionId = :regionId)
              AND (:isAuctionComplete IS NULL OR i.isAuctionComplete = :isAuctionComplete)
        """)
    Slice<ItemPreviewDto> fetchItemPreviewsByFilters(
        @Param("itemIdList") List<Long> itemIdList,
        @Param("sellerId") Long sellerId,
        @Param("categoryId") Long categoryId,
        @Param("tradingMethodId") Long tradingMethodId,
        @Param("regionId") Long regionId,
        @Param("isAuctionComplete") Boolean isAuctionComplete,
        Pageable pageable
    );

    // 지역별 신규 물품 ID 목록 조회
    @Query("""
        SELECT i.itemId
        FROM Item i
        JOIN i.region r
        WHERE i.isAuctionComplete = false AND r.regionId = :regionId
        ORDER BY i.createdAt DESC
        """)
    List<Long> findLatestInProgressItemIdsByRegion(@Param("regionId") Long regionId, Pageable pageable);


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
