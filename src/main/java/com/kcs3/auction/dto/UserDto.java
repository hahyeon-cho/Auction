package com.kcs3.auction.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDto {

    private Long userId;
    private String nickname;
    private String email;
}
