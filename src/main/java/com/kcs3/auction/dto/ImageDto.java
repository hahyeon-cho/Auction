package com.kcs3.auction.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageDto {

    private String imageUrl;

    @Builder
    public ImageDto(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
