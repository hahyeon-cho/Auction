package com.kcs3.auction.service;

import com.kcs3.auction.entity.Item;
import com.kcs3.auction.entity.ItemLike;
import com.kcs3.auction.entity.User;
import com.kcs3.auction.exception.CommonException;
import com.kcs3.auction.exception.ErrorCode;
import com.kcs3.auction.repository.ItemLikeRepository;
import com.kcs3.auction.repository.ItemRepository;
import com.kcs3.auction.utils.AuthUserProvider;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemLikeService {

    private final AuthUserProvider authUserProvider;

    private final ItemRepository itemRepository;
    private final ItemLikeRepository itemLikeRepository;

    public void createItemLike(Long itemId) {
        User user = authUserProvider.getCurrentUser();

        Item item = itemRepository.findById(itemId)
            .orElseThrow(() -> new CommonException(ErrorCode.ITEM_NOT_FOUND));

        if (!itemLikeRepository.existsByUser_UserIdAndItem_ItemId(user.getUserId(), item.getItemId())) { // 중복 체크
            ItemLike itemLike = ItemLike.builder()
                .user(user)
                .item(item)
                .build();

            user.addLike(itemLike);
            itemLikeRepository.save(itemLike);
        }
    }

    public void deleteItemLike(Long itemId) {
        User user = authUserProvider.getCurrentUser();

        Item item = itemRepository.findById(itemId)
            .orElseThrow(() -> new CommonException(ErrorCode.ITEM_NOT_FOUND));

        Optional<ItemLike> itemLike = itemLikeRepository.findByUser_UserIdAndItem_ItemId(user.getUserId(), item.getItemId());

        if (itemLike.isPresent()) {
            user.getItemLikes().remove(itemLike.get());
            itemLikeRepository.delete(itemLike.get());
        }
    }
}
