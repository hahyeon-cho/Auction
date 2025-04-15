package com.kcs3.auction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemAnswerRequestDto {

    @NotBlank
    private String answerContent;

    @Builder
    public ItemAnswerRequestDto(String answerContent) {
        this.answerContent = answerContent;
    }
}
