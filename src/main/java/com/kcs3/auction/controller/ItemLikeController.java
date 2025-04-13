package com.kcs3.auction.controller;

import com.kcs3.auction.dto.ResponseDto;
import com.kcs3.auction.service.ItemLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 물품 찜 관련 API 컨트롤러입니다. <p>
 * 인증된 사용자가 특정 물품을 찜하거나 찜을 취소할 수 있습니다. <p>
 *
 * <pre>
 * POST    /api/v1/auth/auction/{itemid}/like   -물품 찜 등록
 * DELETE  /api/v1/auth/auction/{itemid}/like   -물품 찜 취소
 * </pre>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/auction")
public class ItemLikeController {

    private final ItemLikeService itemLikeService;

    // 찜 등록
    @PostMapping("/{itemid}/like")
    public ResponseDto<Void> createItemLike(@PathVariable("itemid") Long itemid) {
        itemLikeService.createItemLike(itemid);
        return ResponseDto.ok(null);
    }

    // 찜 취소
    @DeleteMapping("/{itemid}/like")
    public ResponseDto<Void> deleteItemLike(@PathVariable("itemid") Long itemid) {
        itemLikeService.deleteItemLike(itemid);
        return ResponseDto.ok(null);
    }
}
