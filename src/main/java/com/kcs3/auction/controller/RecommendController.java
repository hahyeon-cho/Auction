package com.kcs3.auction.controller;

import com.kcs3.auction.dto.RecommendDto;
import com.kcs3.auction.dto.RecommendListResponseDto;
import com.kcs3.auction.dto.ResponseDto;
import com.kcs3.auction.service.RecommendationService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 추천 기능 관련 API 컨트롤러 <p>
 * 파이썬 서버와 통신하여 임베딩 기반 추천을 수행합니다.<p>
 *
 * <pre>
 * endpoints:
 * - POST  /items        : 추천 itemId 목록을 기반으로 상세 DTO 리스트 생성
 * </pre>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/no-auth/auction/recommendations")
public class RecommendController {
    private final RecommendationService recommendationService;
    // 유사 아이템 추천 요청
    @PostMapping("/similar")
    public ResponseDto<RecommendListResponseDto> getSimilarItems(@RequestBody Map<String, Long> request) {
        Long itemId = request.get("itemId");
        List<RecommendDto> list = recommendationService.getSimilarItems(itemId);
        return ResponseDto.ok(RecommendListResponseDto.from(list));
    }
}
