package com.kcs3.auction.controller;

import com.kcs3.auction.dto.MypageListDto;
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
 * 마이페이지 관련 API 컨트롤러 인증된 사용자의 마이페이지 정보를 조회하는 기능을 제공합니다.
 * <pre>
 * endpoints:
 * - GET /like    : 사용자가 좋아요한 물품 목록 조회
 * - GET /auction : 사용자가 등록한 경매 물품 목록 조회
 * - GET /bid     : 사용자가 입찰에 참여한 목록 조회
 * - GET /award   : 사용자가 낙찰받은 물품 목록 조회
 * </pre>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/mypage")
public class MyPageController {

    private final MypageService mypageService;

    // 사용자가 좋아요한 물품 목록 조회
    @GetMapping("/like")
    public ResponseDto<Slice<MypageListDto>> getMyLikedItems(
        @PageableDefault(size = 10) Pageable pageable) {
        return ResponseDto.ok(mypageService.loadLikedItems(pageable));
    }

    // 사용자가 등록한 경매 물품 목록 조회
    @GetMapping("/auction")
    public ResponseDto<Slice<MypageListDto>> getMyRegisteredItems(
        @PageableDefault(size = 10) Pageable pageable) {
        return ResponseDto.ok(mypageService.loadRegisteredItems(pageable));
    }

    // 사용자가 참여한 입찰 목록 조회
    @GetMapping("/bid")
    public ResponseDto<Slice<MypageListDto>> getMyBidItems(
        @PageableDefault(size = 10) Pageable pageable) {
        return ResponseDto.ok(mypageService.loadBidItems(pageable));
    }

    // 사용자가 낙찰받은 물품 목록 조회
    @GetMapping("/award")
    public ResponseDto<Slice<MypageListDto>> getMyAwardedItems(
        @PageableDefault(size = 10) Pageable pageable) {
        return ResponseDto.ok(mypageService.loadAwardedItems(pageable));
    }
}

