package com.kcs3.auction.service;

import com.kcs3.auction.dto.ItemAnswerRequestDto;
import com.kcs3.auction.dto.ItemQuestionRequestDto;
import com.kcs3.auction.entity.ItemAnswer;
import com.kcs3.auction.entity.ItemDetail;
import com.kcs3.auction.entity.ItemQuestion;
import com.kcs3.auction.entity.User;
import com.kcs3.auction.exception.CommonException;
import com.kcs3.auction.exception.ErrorCode;
import com.kcs3.auction.repository.ItemAnswerRepository;
import com.kcs3.auction.repository.ItemQuestionRepository;
import com.kcs3.auction.repository.ItemRepository;
import com.kcs3.auction.utils.AuthUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemQuestionService {

    private final AuthUserProvider authUserProvider;

    private final ItemRepository itemRepository;
    private final ItemQuestionRepository itemQuestionRepository;
    private final ItemAnswerRepository itemAnswerRepository;

    // === 문의글 기능 ===
    @Transactional
    public void createQuestion(Long itemId, ItemQuestionRequestDto requestDto) {
        User user = authUserProvider.getCurrentUser();

        // 물품을 등록한 사용자가 신규 문의글을 등록하지 못하도록 제한
        Long sellerId = itemRepository.findSellerIdByItemId(itemId);
        if (user.getUserId().equals(sellerId)) {
            throw new CommonException(ErrorCode.PERMISSION_DENIED);
        }

        ItemDetail itemDetail = itemRepository.findItemDetailByItemId(itemId)
            .orElseThrow(() -> new CommonException(ErrorCode.ITEM_DETAIL_NOT_FOUND));

        ItemQuestion itemQuestion = ItemQuestion.builder()
            .user(user)
            .itemDetail(itemDetail)
            .questionContent(requestDto.getQuestionContent())
            .build();

        itemDetail.addQna(itemQuestion);
        itemQuestionRepository.save(itemQuestion);
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        User user = authUserProvider.getCurrentUser();

        ItemQuestion itemQuestion = itemQuestionRepository.findById(questionId)
            .orElseThrow(() -> new CommonException(ErrorCode.ITEM_QUESTION_NOT_FOUND));

        // 문의글을 등록한 사용자만 문의글 삭제 가능
        if (!user.getUserId().equals(itemQuestion.getUser().getUserId())) {
            throw new CommonException(ErrorCode.PERMISSION_DENIED);
        }

        ItemDetail itemDetail = itemQuestion.getItemDetail();

        itemDetail.getQnas().remove(itemQuestion);
        itemQuestionRepository.delete(itemQuestion);
    }

    // === 문의 답글 기능 ===
    @Transactional
    public void createQuestionAnswer(Long itemId, Long questionId, ItemAnswerRequestDto requestDto) {
        User user = authUserProvider.getCurrentUser();

        ItemQuestion itemQuestion = itemQuestionRepository.findById(questionId)
            .orElseThrow(() -> new CommonException(ErrorCode.ITEM_QUESTION_NOT_FOUND));

        Long sellerId = itemRepository.findSellerIdByItemId(itemId);

        // 물품을 등록한 사용자이거나 문의글을 등록한 사용자만 답글을 등록할 수 있도록 제한
        if (
            !user.getUserId().equals(sellerId) &&
            !user.getUserId().equals(itemQuestion.getUser().getUserId())
        ) {
            throw new CommonException(ErrorCode.PERMISSION_DENIED);
        }

        ItemAnswer itemAnswer = ItemAnswer.builder()
            .user(user)
            .itemQuestion(itemQuestion)
            .answerContent(requestDto.getAnswerContent())
            .build();

        itemQuestion.addAnswer(itemAnswer);
        itemAnswerRepository.save(itemAnswer);
    }

    @Transactional
    public void deleteQuestionAnswer(Long questionId, Long answerId) {
        User user = authUserProvider.getCurrentUser();

        ItemQuestion itemQuestion = itemQuestionRepository.findById(questionId)
            .orElseThrow(() -> new CommonException(ErrorCode.ITEM_QUESTION_NOT_FOUND));

        ItemAnswer itemAnswer = itemAnswerRepository.findById(answerId)
            .orElseThrow(() -> new CommonException(ErrorCode.ITEM_ANSWER_NOT_FOUND));

        // 문의 답글을 등록한 사용자만 답글 삭제 가능
        if (!user.getUserId().equals(itemAnswer.getUser().getUserId())) {
            throw new CommonException(ErrorCode.PERMISSION_DENIED);
        }

        itemQuestion.getAnswers().remove(itemAnswer);
        itemAnswerRepository.delete(itemAnswer);
    }
}
