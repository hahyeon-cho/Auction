package com.kcs3.auction.dto;

import com.kcs3.auction.entity.ItemQuestion;
import java.util.List;
import java.util.stream.Collectors;
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

    public static QuestionWithAnswerDto from(ItemQuestion q) {
        return QuestionWithAnswerDto.builder()
            .question(ItemQuestionResponseDto.from(q))
            .answers(
                q.getAnswers().stream()
                    .map(ItemAnswerResponseDto::from)
                    .collect(Collectors.toList())
            )
            .build();
    }
}
