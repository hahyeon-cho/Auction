package com.kcs3.auction.controller;

import com.kcs3.auction.dto.CommentRequest;
import com.kcs3.auction.dto.ItemRegisterRequestDto;
import com.kcs3.auction.dto.LikeRequest;
import com.kcs3.auction.dto.QnaPostRequest;
import com.kcs3.auction.dto.ResponseDto;
import com.kcs3.auction.exception.CommonException;
import com.kcs3.auction.exception.ErrorCode;
import com.kcs3.auction.service.ItemService;
import com.kcs3.auction.service.LikeService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/auction")
public class ItemUploadController {

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
    @PostMapping("/{itemid}/qna/{questionid}/")
    public ResponseEntity<NormalResponse> postComment(@RequestBody CommentRequest request, @PathVariable("questionid") long id) {
        itemService.postComment(request, id);
        String message = "문의댓글 등록을 성공하였습니다";
        String status = "success";
        return ResponseEntity.status(HttpStatus.CREATED).body(new NormalResponse(status, message));
    }

    //문의댓글 삭제
    @DeleteMapping("/{itemid}/comment/{CommentId}")
    public ResponseEntity<NormalResponse> deleteComment(@PathVariable("CommentId") long id) {
        itemService.deleteComment(id);
        String message = "문의댓글을 삭제하였습니다";
        String status = "success";
        return ResponseEntity.status(HttpStatus.CREATED).body(new NormalResponse(status, message));
    }

    // 물품 등록
    @PostMapping(value = "/form/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseDto<?> postAuctionItem(
        @RequestPart("data") ItemRegisterRequestDto requestDto,
        @RequestPart("images") List<MultipartFile> images
    ) {
        itemService.registerAuctionItem(requestDto, images);
        return ResponseDto.ok("물품 등록을 성공하였습니다.");
    }


            ResponseEntity<String> result = response.block();
            if (result != null && result.getStatusCode().is2xxSuccessful()) {
                String message = "임베딩 저장을 성공하였습니다";
                String status = "success";
                return ResponseEntity.status(HttpStatus.OK).body(new NormalResponse(status, message));
            } else {
                String message = "임베딩 저장에 실패하였습니다";
                String status = "fail";
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new NormalResponse(status, message));
            }
        } catch (Exception e) {
            String message = "임베딩 저장에 실패하였습니다";
            String status = "fail";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new NormalResponse(status, message));
        }
    }


    //찜목록에 등록
    @PostMapping("/{itemid}/like/")
    public ResponseEntity<NormalResponse> postItemLike(@PathVariable("itemid") long itemid, @RequestBody LikeRequest likeRequest) {
        boolean isLiked = likeService.postLike(itemid, likeRequest.getLikeUserId());
        String message;
        HttpStatus status;

        if (isLiked) {
            message = "찜목록에 등록을 성공하였습니다.";
            status = HttpStatus.CREATED;
        } else {
            message = "이미 찜목록에 등록된 아이템입니다.";
            status = HttpStatus.OK;
        }
        return ResponseEntity.status(status).body(new NormalResponse("success", message));
    }

    //찜목록 삭제
    @DeleteMapping("/{itemid}/like/")
    public ResponseEntity<NormalResponse> deleteItemLike(@PathVariable("itemid") long itemid) {
        boolean isDeleted = likeService.deleteLike(itemid);
        String message = isDeleted ? "찜목록 삭제를 성공하였습니다." : "찜목록 삭제를 실패하였습니다.";
        String status = isDeleted ? "success" : "fail";
        return ResponseEntity.status(HttpStatus.OK).body(new NormalResponse(status, message));
    }
}
