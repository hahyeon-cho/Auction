package com.kcs3.auction.service;

import com.kcs3.auction.dto.ItemPreviewDto;
import com.kcs3.auction.entity.User;
import com.kcs3.auction.repository.AuctionCompleteItemRepository;
import com.kcs3.auction.repository.AuctionInfoRepository;
import com.kcs3.auction.repository.ItemLikeRepository;
import com.kcs3.auction.repository.ItemRepository;
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

    private final ItemRepository itemRepository;
    private final ItemLikeRepository itemLikeRepository;
    private final AuctionInfoRepository auctionInfoRepository;
    private final AuctionCompleteItemRepository auctionCompleteRepository;

    public Slice<ItemPreviewDto> getMyLikedItems(Pageable pageable) {
        User user = authUserProvider.getCurrentUser();
        Slice<Long> itemIdSlice = itemLikeRepository.findItemIdsByUserId(user.getUserId(), pageable);

        if (itemIdSlice.isEmpty()) {
            return new SliceImpl<>(new ArrayList<>(), pageable, false);
        }

        List<Long> itemIdList = itemIdSlice.getContent();
        List<ItemPreviewDto> itemPreviewList = itemRepository.fetchItemPreviewsByFilters(
            itemIdList,
            null,       // sellerId
            null,       // categoryId
            null,       // tradingMethodId
            null,       // regionId
            null,       // isAuctionComplete
            pageable
        ).getContent();

        return new SliceImpl<>(itemPreviewList, pageable, itemIdSlice.hasNext());
    }

    public Slice<ItemPreviewDto> getMyRegisteredItems(Pageable pageable) {
        User user = authUserProvider.getCurrentUser();

        Slice<ItemPreviewDto> itemPreviewSlice = itemRepository.fetchItemPreviewsByFilters(
            null,       // itemIds
            user.getUserId(),
            null,       // categoryId
            null,       // tradingMethodId
            null,       // regionId
            null,       // isAuctionComplete
            pageable
        );

        return itemPreviewSlice;
    }

    // 사용자가 입찰에 참여한 물품 리스트 반환
    // 현재 경매중인 물품 + 낙찰한 물품 + 낙찰에 실패한 물품 목록
    // [Todo] 사용자가 낙찰한 물품은 목록에서 해당 목록에서 안 보이도록 하는 것 고려
    public Slice<ItemPreviewDto> getMyBidItems(Pageable pageable) {
        User user = authUserProvider.getCurrentUser();
        Slice<Long> itemIdSlice = auctionInfoRepository.findItemIdsByUserId(user.getUserId(), pageable);

        if (itemIdSlice.isEmpty()) {
            return new SliceImpl<>(new ArrayList<>(), pageable, false);
        }

        List<Long> itemIdList = itemIdSlice.getContent();
        List<ItemPreviewDto> itemPreviewList = itemRepository.fetchItemPreviewsByFilters(
            itemIdList,
            null,   // sellerId
            null,       // categoryId
            null,       // tradingMethodId
            null,       // regionId
            null,       // isAuctionComplete
            pageable
        ).getContent();

        return new SliceImpl<>(itemPreviewList, pageable, itemIdSlice.hasNext());
    }

    // 사용자가 낙찰받은 상품 리스트 반환
    // AuctionCompleteItem이 ID 조회와 DTO 조회 양쪽에서 사용되어 2회 접근됨
    // [Todo] 현재는 쿼리 재사용성을 위해 중복접근 허용, 추후 성능 이슈 시 리팩토링 고려
    public Slice<ItemPreviewDto> getMyAwardedItems(Pageable pageable) {
        User user = authUserProvider.getCurrentUser();
        Slice<Long> itemIdSlice = auctionCompleteRepository.findItemIdsByUserId(user.getUserId(), pageable);

        if (itemIdSlice.isEmpty()) {
            return new SliceImpl<>(new ArrayList<>(), pageable, false);
        }

        List<Long> itemIdList = itemIdSlice.getContent();
        List<ItemPreviewDto> itemPreviewList = itemRepository.fetchItemPreviewsByFilters(
            itemIdList,
            null,       // sellerId
            null,       // categoryId
            null,       // tradingMethodId
            null,       // regionId
            true,       // isAuctionComplete
            pageable
        ).getContent();

        return new SliceImpl<>(itemPreviewList, pageable, itemIdSlice.hasNext());
    }
}