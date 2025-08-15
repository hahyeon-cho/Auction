package com.kcs3.auction.dto;

import com.kcs3.auction.entity.ItemQuestion;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemQuestionResponseDto {

    private Long questionId;
    private String questionContents;
    private LocalDateTime questionTime;

    @Builder
    public ItemQuestionResponseDto(Long questionId, String questionContents, LocalDateTime questionTime) {
        this.questionId = questionId;
        this.questionContents = questionContents;
        this.questionTime = questionTime;
    }

    public static ItemQuestionResponseDto from(ItemQuestion q) {
        return ItemQuestionResponseDto.builder()
            .questionId(q.getItemQuestionId())
            .questionContents(q.getQuestionContent())
            .questionTime(q.getCreatedAt())
            .build();
    }
}