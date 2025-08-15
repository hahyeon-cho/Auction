package com.kcs3.auction.dto;

import com.kcs3.auction.entity.ItemAnswer;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemAnswerResponseDto {

    private Long answerId;
    private String answerContent;
    private LocalDateTime answerTime;

    @Builder
    public ItemAnswerResponseDto(Long answerId, String answerContent, LocalDateTime answerTime) {
        this.answerId = answerId;
        this.answerContent = answerContent;
        this.answerTime = answerTime;
    }

    public static ItemAnswerResponseDto from(ItemAnswer a) {
        return ItemAnswerResponseDto.builder()
            .answerId(a.getItemAnswerId())
            .answerContent(a.getAnswerContent())
            .answerTime(a.getCreatedAt())
            .build();
    }
}
