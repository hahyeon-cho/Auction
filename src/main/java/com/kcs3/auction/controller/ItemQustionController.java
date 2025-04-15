package com.kcs3.auction.controller;

import com.kcs3.auction.dto.ItemAnswerRequestDto;
import com.kcs3.auction.dto.ItemQuestionRequestDto;
import com.kcs3.auction.dto.ResponseDto;
import com.kcs3.auction.service.ItemQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 물품 문의글 및 답글 관련 API 컨트롤러입니다. <p>
 * 인증된 사용자가 특정 물품에 대해 문의글 및 답글을 등록하거나 삭제할 수 있습니다. <p>
 *
 * <pre>
 * [문의글 관련]
 * - POST    /api/v1/auth/auction/{itemId}/question             : 문의글 등록
 * - DELETE  /api/v1/auth/auction/{itemId}/question/{questionId} : 문의글 삭제
 *
 * [답글 관련]
 * - POST    /api/v1/auth/auction/{itemId}/qna/{questionId}            : 문의 답글 등록
 * - DELETE  /api/v1/auth/auction/{itemId}/qna/{questionId}/{answerId} : 문의 답글 삭제
 * </pre>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/auction")
public class ItemQustionController {

    private final ItemQuestionService itemQuestionService;

    // === 문의글 기능 ===
    // 문의글 등록
    @PostMapping("/{itemId}/question")
    public ResponseDto<String> createQuestion(
        @PathVariable("itemId") Long itemId,
        @RequestBody ItemQuestionRequestDto requestDto
    ) {
        itemQuestionService.createQuestion(itemId, requestDto);
        return ResponseDto.ok("문의글이 등록되었습니다.");
    }

    // 문의글 삭제
    @DeleteMapping("/{itemId}/question/{questionId}")
    public ResponseDto<String> deleteQna(
        @PathVariable("itemId") Long itemId,
        @PathVariable("questionid") Long questionId
    ) {
        itemQuestionService.deleteQuestion(questionId);
        return ResponseDto.ok("문의글이 삭제되었습니다.");
    }

    // === 문의 답글 기능 ===
    // 문의 답글 등록
    @PostMapping("/{itemid}/qna/{questionId}")
    public ResponseDto<String> createQuestionAnswer(
        @PathVariable("itemId") Long itemId,
        @PathVariable("questionId") Long questionId,
        @RequestBody ItemAnswerRequestDto requestDto
    ) {
        itemQuestionService.createQuestionAnswer(itemId, questionId, requestDto);
        return ResponseDto.ok("답글이 등록되었습니다.");
    }

    // 문의 답글 삭제
    @DeleteMapping("/{itemId}/qna/{questionId}/{answerId}")
    public ResponseDto<String> deleteQuestionAnswer(
        @PathVariable("questionId") Long questionId,
        @PathVariable("answerId") Long answerId
    ) {
        itemQuestionService.deleteQuestionAnswer(questionId, answerId);
        return ResponseDto.ok("답글이 삭제되었습니다.");
    }
}
