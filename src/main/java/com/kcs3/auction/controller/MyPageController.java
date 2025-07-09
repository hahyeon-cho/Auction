package com.kcs3.auction.controller;

import com.kcs3.auction.dto.ItemPreviewDto;
import com.kcs3.auction.dto.ResponseDto;
import com.kcs3.auction.service.MypageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 마이페이지 관련 API 컨트롤러 <p>
 * 인증된 사용자의 마이페이지 정보를 조회하는 기능을 제공합니다. <p>
 *
 * <pre>
 * endpoints:
 * - GET  /auth/mypage/like    : 사용자가 좋아요한 물품 목록 조회
 * - GET  /auth/mypage/auction : 사용자가 등록한 경매 물품 목록 조회
 * - GET  /auth/mypage/bid     : 사용자가 입찰에 참여한 목록 조회
 * - GET  /auth/mypage/award   : 사용자가 낙찰받은 물품 목록 조회
 * </pre>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/mypage")
public class MyPageController {

    private final MypageService mypageService;

    // 사용자가 좋아요한 물품 목록 조회
    @GetMapping("/like")
    public ResponseDto<Slice<ItemPreviewDto>> getMyLikedItems(
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseDto.ok(mypageService.getMyLikedItems(pageable));
    }

    // 사용자가 등록한 경매 물품 목록 조회
    @GetMapping("/auction")
    public ResponseDto<Slice<ItemPreviewDto>> getMyRegisteredItems(
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseDto.ok(mypageService.getMyRegisteredItems(pageable));
    }

    // 사용자가 참여한 입찰 목록 조회
    @GetMapping("/bid")
    public ResponseDto<Slice<ItemPreviewDto>> getMyBidItems(
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseDto.ok(mypageService.getMyBidItems(pageable));
    }

    // 사용자가 낙찰받은 물품 목록 조회
    @GetMapping("/award")
    public ResponseDto<Slice<ItemPreviewDto>> getMyAwardedItems(
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseDto.ok(mypageService.getMyAwardedItems(pageable));
    }
}

