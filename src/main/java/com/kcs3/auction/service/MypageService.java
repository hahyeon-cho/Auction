package com.kcs3.auction.service;

import com.kcs3.auction.dto.MypageListDto;
import com.kcs3.auction.entity.AuctionCompleteItem;
import com.kcs3.auction.entity.AuctionProgressItem;
import com.kcs3.auction.entity.Item;
import com.kcs3.auction.entity.LikeItem;
import com.kcs3.auction.entity.User;
import com.kcs3.auction.repository.AuctionCompleteItemRepository;
import com.kcs3.auction.repository.AuctionInfoRepository;
import com.kcs3.auction.repository.AuctionProgressItemRepository;
import com.kcs3.auction.repository.ItemRepository;
import com.kcs3.auction.repository.LikeItemRepository;
import com.kcs3.auction.utils.AuthUserProvider;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MypageService {

    private final AuthUserProvider authUserProvider;

    private final LikeItemRepository likeItemRepository;
    private final ItemRepository itemRepository;
    private final AuctionInfoRepository auctionInfoRepository;
    private final AuctionProgressItemRepository auctionProgressRepository;
    private final AuctionCompleteItemRepository auctionCompleteRepository;

    public Slice<MypageListDto> loadLikedItems(Pageable pageable) {
        User user = authUserProvider.getCurrentUser();
        Slice<LikeItem> likedItems = likeItemRepository.findByUser(user, pageable);
        List<MypageListDto> result = new ArrayList<>();

        for (LikeItem likeItem : likedItems) {
            Item item = likeItem.getItem();
            if (item != null && !item.isAuctionComplete()) {
                AuctionProgressItem progressItem = auctionProgressRepository.findAuctionProgressItemByItem(
                    item);
                result.add(MypageListDto.fromProgressEntity(item, progressItem));
            } else if (item != null) {
                AuctionCompleteItem completeItem = auctionCompleteRepository.findCompleteItemByItem(
                    item);
                result.add(MypageListDto.fromCompleteEntity(item, completeItem));
            }
        }

        return new SliceImpl<>(result, pageable, likedItems.hasNext());
    }

    public Slice<MypageListDto> loadRegisteredItems(Pageable pageable) {
        User user = authUserProvider.getCurrentUser();
        Slice<Item> userItems = itemRepository.findBySeller(user, pageable);
        List<MypageListDto> result = new ArrayList<>();

        for (Item item : userItems) {
            if (item != null && !item.isAuctionComplete()) {
                AuctionProgressItem progressItem = auctionProgressRepository.findAuctionProgressItemByItem(
                    item);
                result.add(MypageListDto.fromProgressEntity(item, progressItem));
            } else if (item != null) {
                AuctionCompleteItem completeItem = auctionCompleteRepository.findCompleteItemByItem(
                    item);
                result.add(MypageListDto.fromCompleteEntity(item, completeItem));
            }
        }

        return new SliceImpl<>(result, pageable, userItems.hasNext());
    }

    public Slice<MypageListDto> loadBidItems(Pageable pageable) {
        User user = authUserProvider.getCurrentUser();
        Slice<Item> items = auctionInfoRepository.findByUser(user, pageable);
        List<MypageListDto> result = new ArrayList<>();

        for (Item item : items) {
            AuctionProgressItem progressItem = auctionProgressRepository.findAuctionProgressItemByItem(
                item);
            if (progressItem != null) {
                result.add(MypageListDto.fromEntity(progressItem));
            }
        }

        return new SliceImpl<>(result, pageable, items.hasNext());
    }

    // 유저가 낙찰받은 상품 리스트 반환
    public Slice<MypageListDto> loadAwardedItems(Pageable pageable) {
        User user = authUserProvider.getCurrentUser();
        Slice<AuctionCompleteItem> completeItems = auctionCompleteRepository.findByUser(user,
            pageable);
        List<MypageListDto> result = new ArrayList<>();

        for (AuctionCompleteItem item : completeItems) {
            result.add(MypageListDto.fromEntity(item));
        }

        return new SliceImpl<>(result, pageable, completeItems.hasNext());
    }
}