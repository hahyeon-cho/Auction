package com.kcs3.auction.controller;

import com.kcs3.auction.dto.ItemPreviewDto;
import com.kcs3.auction.dto.ResponseDto;
import com.kcs3.auction.service.ItemSearchService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/no-auth")
public class ItemSearchController {

    private ItemSearchService itemSearchService;

    /**
     * 경매 물품 목록 조회
     * <p>
     * 필터 조건에 따라 물품을 검색하며, 페이징 처리된 결과를 반환합니다.
     *
     * @param pageable      페이지 정보 (기본 20개씩)
     * @param keyword       검색 키워드 (선택, 제목)
     * @param category      카테고리 이름 (선택)
     * @param tradingMethod 거래 방식 코드 (선택)
     * @param region        지역 이름 (선택)
     * @param status        경매 종료 여부 (true: 종료, false: 진행중, null: 전체)
     * @return ResponseDto<Slice < ItemPreviewDto>> 페이징된 물품 미리보기 목록
     */
    @GetMapping("/auction")
    public ResponseDto<Slice<ItemPreviewDto>> getAuctionItemList(
        @PageableDefault(size = 20) Pageable pageable,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) Integer tradingMethod,
        @RequestParam(required = false) String region,
        @RequestParam(required = false) Boolean status
    ) {
        return ResponseDto.ok(
            itemSearchService.searchItemList(keyword, category, tradingMethod, region, status, pageable));
    }

    /**
     * 물품 상세정보 조회 api
     */
    @GetMapping("/auction/{itemId}")
    public ResponseEntity<NormalResponse> getItemDetail(@PathVariable Long itemId) {
        ItemDetailRequestDto itemDetail = itemService.getItemDetail(itemId);
        String message = "아이템 상세 정보를 성공적으로 가져왔습니다";
        String status = "success";
        NormalResponse response = new NormalResponse(status, message, itemDetail);
        return ResponseEntity.ok(response);
    }


}
