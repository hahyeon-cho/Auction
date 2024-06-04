package com.kcs3.auction.controller;

import com.kcs3.auction.dto.HotItemListDto;
import com.kcs3.auction.dto.ItemDetailRequestDto;
import com.kcs3.auction.dto.NormalResponse;
import com.kcs3.auction.dto.ProgressItemListDto;
import com.kcs3.auction.dto.ResponseDto;
import com.kcs3.auction.service.ItemListService;
import com.kcs3.auction.service.ItemService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@AllArgsConstructor
@RequestMapping("api/v1/no-auth")
public class ItemSearchController {

    private ItemListService itemListService;

    private ItemService itemService;



    /**
     * 경매진행중인 아이템 목록 조회 - API
     * 경매완료된 아이템 목록 조회
     * 전체 경매 아이템 목록 조회
     */
    @GetMapping("/auction")
    public ResponseDto<ProgressItemListDto> getProgressItemsApi(@PageableDefault(size = 10)Pageable pageable,
                                                                @RequestParam(required = false) String category,
                                                                @RequestParam(required = false) Integer tradingMethod,
                                                                @RequestParam(required = false) String region,
                                                                @RequestParam String status
                                                                ) {
        return ResponseDto.ok(itemListService.getProgressItems(category, tradingMethod, region, status, pageable));

    }

    /**
     * Redis에서 Hot Item 목록 조회 - API
     */
    @GetMapping("/hot-item")
    public ResponseDto<HotItemListDto> getHotItemsSaveApi() {
        return ResponseDto.ok(itemListService.getHotItems());
    }

    /**
     * Redis에서 New Item 목록 조회 - API
     */
    @GetMapping("/new-item")
    public ResponseDto<HotItemListDto> getNewItemsSaveApi() {
        return ResponseDto.ok(itemListService.getNewItems());
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
