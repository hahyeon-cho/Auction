package com.kcs3.auction.repository;

import com.kcs3.auction.entity.ItemLike;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ItemLikeRepository extends JpaRepository<ItemLike, Long> {

    // 사용자 ID와 물품 ID로 찜 여부 확인
    boolean existsByUser_UserIdAndItem_ItemId(Long userId, Long itemId);

    // 사용자 ID와 물품 ID로 찜 객체 반환
    Optional<ItemLike> findByUser_UserIdAndItem_ItemId(Long userId, Long itemId);

    // 사용자 ID 기준으로 좋아요한 물품 ID 목록 조회 (최신순)
    @Query("SELECT l.item.itemId FROM ItemLike l WHERE l.user.userId = :userId ORDER BY l.createdAt DESC")
    Slice<Long> findItemIdsByUserId(@Param("userId") Long userId, Pageable pageable);

}
