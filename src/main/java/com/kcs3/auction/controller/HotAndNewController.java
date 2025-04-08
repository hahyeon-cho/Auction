package com.kcs3.auction.controller;

import com.kcs3.auction.dto.RedisItemListDto;
import com.kcs3.auction.dto.ResponseDto;
import com.kcs3.auction.service.HotAndNewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/no-auth")
public class HotAndNewController {

    private final HotAndNewService hotAndNewService;

    // Redis에서 Hot Item 목록 조회
    @GetMapping("/hot-item")
    public ResponseDto<RedisItemListDto> getHotAuctionItems(@RequestParam(required = false) String regionName) {
        return ResponseDto.ok(hotAndNewService.getHotItemsByRegion(regionName));
    }

    // Redis에서 New Item 목록 조회
    @GetMapping("/new-item")
    public ResponseDto<RedisItemListDto> getNewAuctionItems(@RequestParam(required = false) String regionName) {
        return ResponseDto.ok(hotAndNewService.getNewItemsByRegion(regionName));
    }
}
