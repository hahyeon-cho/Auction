package com.kcs3.auction.controller;

import com.kcs3.auction.dto.CommentRequest;
import com.kcs3.auction.dto.LikeRequest;
import com.kcs3.auction.dto.ItemQuestionRequestDto;
import com.kcs3.auction.dto.ResponseDto;
import com.kcs3.auction.entity.ItemDetail;
import com.kcs3.auction.entity.ItemQuestion;
import com.kcs3.auction.entity.ItemQuestionAnswer;
import com.kcs3.auction.exception.CommonException;
import com.kcs3.auction.exception.ErrorCode;
import com.kcs3.auction.service.ItemService;
import com.kcs3.auction.service.ItemSupportService;
import com.kcs3.auction.service.LikeService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/auction")
public class ItemSupportController {

    private final ItemService itemService;
    private final LikeService likeService;

    //문의글 등록
    @PostMapping("/{itemid}/qna/")
    public ResponseEntity<NormalResponse> postQna(@RequestBody QnaPostRequest request, @PathVariable("itemid") long id) {
        itemService.postQna(request, id);
        String message = "문의글 등록을 성공하였습니다";
        String status = "success";
        return ResponseEntity.status(HttpStatus.CREATED).body(new NormalResponse(status, message));
    }

    //문의글 삭제
    @DeleteMapping("/{itemid}/qna/{questionid}/")
    public ResponseEntity<NormalResponse> deleteQna(@PathVariable("questionid") long questionid) {
        itemService.deleteQna(questionid);
        String message = "문의글 삭제를 성공하였습니다";
        String status = "success";
        return ResponseEntity.status(HttpStatus.CREATED).body(new NormalResponse(status, message));
    }



    //문의댓글 등록
    @PostMapping("/{itemid}/qna/{questionid}")
    public ResponseDto<?> postComment(@RequestBody CommentRequest request,
        @PathVariable("questionid") long id) {
        itemService.postComment(request, id);
        return ResponseDto.ok(null);
    }

    //문의댓글 삭제
    @DeleteMapping("/{itemid}/comment/{CommentId}")
    public ResponseDto<Void> deleteComment(@PathVariable("CommentId") long id) {
        itemService.deleteComment(id);
        return ResponseDto.ok(null);
    }




    // 찜 등록
    @PostMapping("/{itemid}/like")
    public ResponseDto<?> postItemLike(@PathVariable("itemid") long itemid,
        @RequestBody LikeRequest likeRequest) {
        boolean isLiked = likeService.postLike(itemid, likeRequest.getLikeUserId());
        return ResponseDto.ok(null);
    }

    // 찜 삭제
    @DeleteMapping("/{itemid}/like")
    public ResponseDto<Void> deleteItemLike(@PathVariable("itemid") long itemid) {
        boolean isDeleted = likeService.deleteLike(itemid);
        if (!isDeleted) {
            throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        return ResponseDto.ok(null);
    }


    private ItemDetail findDetailByItemId(Long itemId) {
        Optional<ItemDetail> OitemDetail = itemDetailRepository.findByItemId(itemId);
        if (OitemDetail.isPresent()) {
            return OitemDetail.get();
        }
        return null;
    }

    private ItemQuestion findItemQuestionById(long questionId) {
        Optional<ItemQuestion> OitemQuestion = itemQuestionRepository.findById(questionId);
        if (OitemQuestion.isPresent()) {
            return OitemQuestion.get();
        }
        return null;
    }

    private ItemQuestionAnswer findCommentById(long commentId) {
        Optional<ItemQuestionAnswer> optionalQnaComment = qnaCommentRepository.findById(commentId);
        return optionalQnaComment.orElse(null);
    }

}
