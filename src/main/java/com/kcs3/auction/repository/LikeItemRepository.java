package com.kcs3.auction.repository;


import com.kcs3.auction.entity.Item;
import com.kcs3.auction.entity.LikeItem;
import com.kcs3.auction.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeItemRepository extends JpaRepository<LikeItem, Long> {

    Optional<LikeItem> findByItem_ItemIdAndUser(Long itemId, User user);

    boolean existsByUserAndItem(User user, Item item);

}
