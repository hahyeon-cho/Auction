package com.kcs3.auction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemQuestionRequestDto {

    @NotBlank
    private String questionContent;

    @Builder
    public ItemQuestionRequestDto(String questionContent) {
        this.questionContent = questionContent;
    }
}
