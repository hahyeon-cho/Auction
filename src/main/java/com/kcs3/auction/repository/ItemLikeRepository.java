package com.kcs3.auction.repository;

import com.kcs3.auction.entity.Item;
import com.kcs3.auction.entity.ItemLike;
import com.kcs3.auction.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ItemLikeRepository extends JpaRepository<ItemLike, Long> {

    // 사용자 ID 기준으로 좋아요한 아이템 ID 목록 조회 (최신순)
    @Query("SELECT l.item.itemId FROM ItemLike l WHERE l.userId = :userId ORDER BY l.createdAt DESC")
    Slice<Long> findItemIdsByUserId(@Param("userId") Long userId, Pageable pageable);


    // 물품 ID와 사용자 정보로 찜 정보 조회
    Optional<ItemLike> findByItem_ItemIdAndUser(Long itemId, User user);

    Optional<ItemLike> findByUserAndItem(User user, Item item);

    // 사용자와 물품으로 찜 여부 확인
    boolean existsByUserAndItem(User user, Item item);

    Slice<ItemLike> findByUser(User user, Pageable pageable);
}
