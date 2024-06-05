package com.kcs3.auction.controller;

import com.kcs3.auction.dto.ProgressItemListDto;
import com.kcs3.auction.dto.ResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/health")
public class HealthCheckController {

    @GetMapping
    public ResponseDto<Boolean> getHealthCheck(){
        return ResponseDto.ok(true);
    }
}
