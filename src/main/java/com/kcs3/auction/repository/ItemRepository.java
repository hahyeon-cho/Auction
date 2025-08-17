package com.kcs3.auction.repository;

import com.kcs3.auction.dto.ItemPreviewDto;
import com.kcs3.auction.entity.Item;
import com.kcs3.auction.entity.ItemDetail;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // 물품 ID로 판매자 ID 조회
    @Query("SELECT i.seller.userId FROM Item i WHERE i.itemId = :itemId")
    Long findSellerIdByItemId(@Param("itemId") Long itemId);

    // 물품 ID로 상세 정보 조회
    Optional<ItemDetail> findItemDetailByItemId(Long itemId);

    // 필터 조건(카테고리, 거래방식, 지역, 경매상태 등)으로 경매 물품 목록 조회
    // 경매 상태에 따라 경매 중 테이블/경매 완료 테이블 선택 조인
    // [TODO] 쿼리 재사용성을 위해 공통 조회 구조 유지 중. 성능이슈 또는 조건 복잡도 증가 시 분리 또는 QueryDSL 리팩토링 고려
    @Query(value = """
            SELECT new com.kcs3.auction.dto.ItemPreviewDto(
                i.id,
                COALESCE(api.itemTitle, aci.itemTitle),
                COALESCE(api.thumbnail, aci.thumbnail),
                c.categoryName,
                tm.tmCode,
                COALESCE(api.location, aci.location),
                COALESCE(api.startPrice, aci.startPrice),
                COALESCE(api.buyNowPrice, aci.buyNowPrice),
                COALESCE(api.maxPrice, aci.maxPrice),
                COALESCE(api.bidFinishTime, aci.bidFinishTime),
                i.isAuctionComplete,
                aci.isBidComplete
            )
            FROM Item i
            JOIN i.category c
            JOIN i.tradingMethod tm
            LEFT JOIN AuctionProgressItem api ON i.isAuctionComplete = false AND api.item = i
            LEFT JOIN AuctionCompleteItem aci ON i.isAuctionComplete = true AND aci.item = i
            WHERE (:itemIdList IS NULL OR i.itemId IN :itemIdList)
              AND (:sellerId IS NULL OR i.seller.id = :sellerId)
              AND (:categoryId IS NULL OR i.category.id = :categoryId)
              AND (:tradingMethodId IS NULL OR i.tradingMethod.id = :tradingMethodId)
              AND (:regionId IS NULL OR i.region.regionId = :regionId)
              AND (:isAuctionComplete IS NULL OR i.isAuctionComplete = :isAuctionComplete)
            ORDER BY i.createdAt DESC
        """, countQuery = """
            SELECT COUNT(i)
            FROM Item i
            WHERE (:itemIdList IS NULL OR i.itemId IN :itemIdList)
              AND (:sellerId IS NULL OR i.seller.id = :sellerId)
              AND (:categoryId IS NULL OR i.category.id = :categoryId)
              AND (:tradingMethodId IS NULL OR i.tradingMethod.id = :tradingMethodId)
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

    // 지정된 ID 리스트 순서를 유지하여 경매 물품 목록 조회
    @Query(value = """
           SELECT new com.kcs3.auction.dto.ItemPreviewDto(
               i.id,
               COALESCE(api.itemTitle, aci.itemTitle),
               COALESCE(api.thumbnail, aci.thumbnail),
               c.categoryName,
               tm.tmCode,
               COALESCE(api.location, aci.location),
               COALESCE(api.startPrice, aci.startPrice),
               COALESCE(api.buyNowPrice, aci.buyNowPrice),
               COALESCE(api.maxPrice, aci.maxPrice),
               COALESCE(api.bidFinishTime, aci.bidFinishTime),
               i.isAuctionComplete,
               aci.isBidComplete
           )
            FROM Item i
            JOIN i.category c
            JOIN i.tradingMethod tm
            LEFT JOIN AuctionProgressItem api ON i.isAuctionComplete = false AND api.item = i
            LEFT JOIN AuctionCompleteItem aci ON i.isAuctionComplete = true AND aci.item = i
            WHERE i.itemId IN :itemIdList
            AND (:isAuctionComplete IS NULL OR i.isAuctionComplete = :isAuctionComplete)
            ORDER BY FIELD(i.itemId, :itemIdList)
        """)
    Slice<ItemPreviewDto> fetchItemPreviewsByItemIdOrder(
        @Param("itemIdList") List<Long> itemIdList,
        @Param("isAuctionComplete") Boolean isAuctionComplete,
        Pageable pageable
    );

    // LIKE 검색을 통해 물품 ID 목록 조회
    @Query("""
        SELECT i.id
        FROM Item i
        LEFT JOIN AuctionProgressItem api ON i.isAuctionComplete = false AND api.item = i
        LEFT JOIN AuctionCompleteItem aci ON i.isAuctionComplete = true AND aci.item = i
        WHERE (COALESCE(api.itemTitle, aci.itemTitle) LIKE %:keyword%)
        ORDER BY i.createdAt DESC
    """)
    List<Long> findItemIdsByKeyword(
        @Param("keyword") String keyword,
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
}
