package com.kcs3.auction.controller;

import com.kcs3.auction.dto.ItemCreateRequestDto;
import com.kcs3.auction.dto.ItemDetailResponseDto;
import com.kcs3.auction.dto.ResponseDto;
import com.kcs3.auction.service.ItemService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/auction")
public class ItemController {

    private final ItemService itemService;

    // 경매 물품 등록
    @PostMapping(value = "/form", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto<String> createAuctionItem(
        @RequestPart("data") ItemCreateRequestDto requestDto,
        @RequestPart("images") List<MultipartFile> images
    ) {
        itemService.createAuctionItem(requestDto, images);
        return ResponseDto.ok("물품 등록을 성공하였습니다.");
    }
}
