package com.kcs3.auction.service;

import static com.kcs3.auction.support.TestFixture.item;
import static com.kcs3.auction.support.TestFixture.user;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;

import com.kcs3.auction.entity.Item;
import com.kcs3.auction.entity.ItemLike;
import com.kcs3.auction.entity.User;
import com.kcs3.auction.exception.CommonException;
import com.kcs3.auction.exception.ErrorCode;
import com.kcs3.auction.repository.ItemLikeRepository;
import com.kcs3.auction.repository.ItemRepository;
import com.kcs3.auction.utils.AuthUserProvider;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemLikeService")
class ItemLikeServiceTest {

    @Mock private AuthUserProvider authUserProvider;
    @Mock private ItemRepository itemRepository;
    @Mock private ItemLikeRepository itemLikeRepository;

    @InjectMocks private ItemLikeService itemLikeService;

    private User user;
    private Item item;

    private static final Long USER_ID = 1L;
    private static final Long ITEM_ID = 100L;

    @BeforeEach
    void setUp() {
        user = user(USER_ID, "user", "user@test.com");
        item = item(ITEM_ID, user);
    }

    @Nested
    @DisplayName("좋아요 등록")
    class CreateItemLike {

        @Test
        @DisplayName("좋아요 없음 → 좋아요 등록")
        void createLikeSuccess() {
            // given
            given(authUserProvider.getCurrentUser()).willReturn(user);
            given(itemRepository.findById(ITEM_ID)).willReturn(Optional.of(item));
            given(itemLikeRepository.existsByUser_UserIdAndItem_ItemId(USER_ID, ITEM_ID))
                .willReturn(false);

            // when
            itemLikeService.createItemLike(ITEM_ID);

            // then
            then(itemLikeRepository).should().save(any(ItemLike.class));
        }

        @Test
        @DisplayName("이미 좋아요 존재 → 아무 동작 없음")
        void createLikeAlreadyExists() {
            // given
            given(authUserProvider.getCurrentUser()).willReturn(user);
            given(itemRepository.findById(ITEM_ID)).willReturn(Optional.of(item));
            given(itemLikeRepository.existsByUser_UserIdAndItem_ItemId(USER_ID, ITEM_ID))
                .willReturn(true);

            // when
            itemLikeService.createItemLike(ITEM_ID);

            // then
            then(itemLikeRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("아이템 없음 → 좋아요 등록 실패")
        void createLikeItemNotFound() {
            // given
            given(authUserProvider.getCurrentUser()).willReturn(user);
            given(itemRepository.findById(anyLong())).willReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> itemLikeService.createItemLike(ITEM_ID))
                .isInstanceOf(CommonException.class)
                .hasFieldOrPropertyWithValue(
                    "errorCode",
                    ErrorCode.ITEM_NOT_FOUND
                );

            then(itemLikeRepository).should(never()).save(any());
        }
    }

    @Nested
    @DisplayName("좋아요 삭제")
    class DeleteItemLike {

        @Test
        @DisplayName("좋아요 존재 → 삭제 성공")
        void deleteLikeSuccess() {
            // given
            ItemLike itemLike = ItemLike.builder()
                .user(user)
                .item(item)
                .build();

            given(authUserProvider.getCurrentUser()).willReturn(user);
            given(itemRepository.findById(ITEM_ID)).willReturn(Optional.of(item));
            given(itemLikeRepository.findByUser_UserIdAndItem_ItemId(USER_ID, ITEM_ID))
                .willReturn(Optional.of(itemLike));

            // when
            itemLikeService.deleteItemLike(ITEM_ID);

            // then
            then(itemLikeRepository).should().delete(itemLike);
        }

        @Test
        @DisplayName("좋아요 없음 → 아무 동작 없음")
        void deleteLikeNotExists() {
            // given
            given(authUserProvider.getCurrentUser()).willReturn(user);
            given(itemRepository.findById(ITEM_ID)).willReturn(Optional.of(item));
            given(itemLikeRepository.findByUser_UserIdAndItem_ItemId(USER_ID, ITEM_ID))
                .willReturn(Optional.empty());

            // when
            itemLikeService.deleteItemLike(ITEM_ID);

            // then
            then(itemLikeRepository).should(never()).delete(any());
        }

        @Test
        @DisplayName("아이템 없음 → 좋아요 삭제 실패")
        void deleteLikeItemNotFound() {
            // given
            given(authUserProvider.getCurrentUser()).willReturn(user);
            given(itemRepository.findById(anyLong())).willReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> itemLikeService.deleteItemLike(ITEM_ID))
                .isInstanceOf(CommonException.class)
                .hasFieldOrPropertyWithValue(
                    "errorCode",
                    ErrorCode.ITEM_NOT_FOUND
                );

            then(itemLikeRepository).should(never()).delete(any());
        }
    }
}
