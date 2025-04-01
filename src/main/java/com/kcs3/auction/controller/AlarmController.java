package com.kcs3.auction.controller;

import com.kcs3.auction.dto.ResponseDto;
import com.kcs3.auction.service.AlarmService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/auction")
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping("/alarm")
    public ResponseDto<List<String>> getAlarm() {
        return ResponseDto.ok(alarmService.loadAlarm());
    }
}