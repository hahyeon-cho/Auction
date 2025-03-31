package com.kcs3.auction.repository;

import com.kcs3.auction.entity.Item;
import com.kcs3.auction.entity.LikeItem;
import com.kcs3.auction.entity.User;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeItemRepository extends JpaRepository<LikeItem, Long> {

    // 물품 ID와 사용자 정보로 찜 정보 조회
    Optional<LikeItem> findByItem_ItemIdAndUser(Long itemId, User user);

    Optional<LikeItem> findByUserAndItem(User user, Item item);

    // 유저와 물품으로 찜 여부 확인
    boolean existsByUserAndItem(User user, Item item);

    Slice<LikeItem> findByUser(User user, Pageable pageable);
}
