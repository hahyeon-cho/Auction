package com.kcs3.auction.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionWithAnswerDto {

    private ItemQuestionResponseDto question;
    private List<ItemAnswerResponseDto> answers;

    @Builder
    public QuestionWithAnswerDto(ItemQuestionResponseDto question, List<ItemAnswerResponseDto> answers) {
        this.question = question;
        this.answers = answers;
    }
}
