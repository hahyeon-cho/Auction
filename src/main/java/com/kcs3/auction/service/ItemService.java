package com.kcs3.auction.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.kcs3.auction.document.ItemDocument;
import com.kcs3.auction.dto.CommentRequest;
import com.kcs3.auction.dto.ItemDetailRequestDto;
import com.kcs3.auction.dto.ItemRegisterRequestDto;
import com.kcs3.auction.dto.QnaPostRequest;
import com.kcs3.auction.dto.RecommendDto;
import com.kcs3.auction.entity.Alarm;
import com.kcs3.auction.entity.AuctionCompleteItem;
import com.kcs3.auction.entity.AuctionProgressItem;
import com.kcs3.auction.entity.Category;
import com.kcs3.auction.entity.Item;
import com.kcs3.auction.entity.ItemDetail;
import com.kcs3.auction.entity.ItemImage;
import com.kcs3.auction.entity.ItemQuestion;
import com.kcs3.auction.entity.QnaComment;
import com.kcs3.auction.entity.Region;
import com.kcs3.auction.entity.TradingMethod;
import com.kcs3.auction.entity.User;
import com.kcs3.auction.exception.CommonException;
import com.kcs3.auction.exception.ErrorCode;
import com.kcs3.auction.repository.AlarmRepository;
import com.kcs3.auction.repository.AuctionCompleteItemRepository;
import com.kcs3.auction.repository.AuctionProgressItemRepository;
import com.kcs3.auction.repository.CategoryRepository;
import com.kcs3.auction.repository.ItemDetailRepository;
import com.kcs3.auction.repository.ItemElasticsearchRepository;
import com.kcs3.auction.repository.ItemQuestionRepository;
import com.kcs3.auction.repository.ItemRepository;
import com.kcs3.auction.repository.QnaCommentRepository;
import com.kcs3.auction.repository.RegionRepository;
import com.kcs3.auction.repository.TradingMethodRepository;
import com.kcs3.auction.repository.UserRepository;
import com.kcs3.auction.utils.AuthUserProvider;
import com.kcs3.auction.utils.CustomOAuth2User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Log4j2
public class ItemService {

    private final AuthUserProvider authUserProvider;

    private final AmazonS3 amazonS3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.AP_NORTHEAST_2) // 서울 리전
        .build();

    private final RecommendationService itemRecommendationService;

    private final UserRepository userRepository;

    private final ItemRepository itemRepository;
    private final ItemDetailRepository itemDetailRepository;

    private final ItemQuestionRepository itemQuestionRepository;
    private final QnaCommentRepository qnaCommentRepository;

    private final AuctionProgressItemRepository auctionProgressItemRepository;
    private final AuctionCompleteItemRepository auctionCompleteItemRepository;


    private final AlarmRepository alarmRepository;

    private final ItemElasticsearchRepository itemElasticsearchRepository;


    @Value("${cloud.aws.s3.bucketName}")
    private String bucket;

    private final CategoryRepository categoryRepository;
    private final TradingMethodRepository tradingMethodRepository;
    private final RegionRepository regionRepository;







    public List<String> getAlarm() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        User user = userRepository.findByUserId(customOAuth2User.getUserId())
            .orElseThrow(() -> new CommonException(ErrorCode.USER_NOT_FOUND));

        List<Alarm> alarms = alarmRepository.findTop4ByUserOrderByCreatedAtDesc(user);

        return alarms.stream().map(Alarm::getAlarmContent).collect(Collectors.toList());

    }

    public void postQna(QnaPostRequest request, Long itemId) {

        ItemQuestion itemQuestion = ItemQuestion.builder().itemDetailId(this.findDetailByItemId(itemId)) // 아이템 상세 ID 설정
            .questionContents(request.getQuestionContents()) // 질문 내용 설정
            .questionUserId(request.getQuestionUserId()) // 질문 작성자 ID 설정
            .build();

        this.itemQuestionRepository.save(itemQuestion);
    }


    public void deleteQna(Long questionId) {
        ItemQuestion itemQuestion = this.findItemQuestionById(questionId);
        if (itemQuestion != null) {
            this.itemQuestionRepository.delete(itemQuestion);
        }
    }


    public void postComment(CommentRequest request, Long questionId) {

        QnaComment comment = QnaComment.builder().questionId(this.findItemQuestionById(questionId))
            .comment(request.getComment()).build();

        this.qnaCommentRepository.save(comment);
    }


    public boolean deleteComment(Long commentId) {
        QnaComment qnaComment = this.findCommentById(commentId);
        if (qnaComment != null) {
            this.qnaCommentRepository.delete(qnaComment);
            return true;
        }
        return false;
    }


    // 물품 등록 서비스
    @Transactional
    public void registerAuctionItem(ItemRegisterRequestDto requestDto, List<MultipartFile> images) {

        User user = authUserProvider.getCurrentUser();

        Category category = categoryRepository.findByCategory(requestDto.getCategory())
            .orElseThrow(() -> new CommonException(ErrorCode.CATEGORY_NOT_FOUND));

        TradingMethod tradingMethod = tradingMethodRepository.findByTradingMethod(requestDto.getTradingMethod())
            .orElseThrow(() -> new CommonException(ErrorCode.TRADING_METHOD_NOT_FOUND));

        Region region = regionRepository.findByRegion(requestDto.getRegion())
            .orElseGet(() -> regionRepository.findByRegion("기타").get());

        // 상세 정보 생성
        ItemDetail itemDetail = ItemDetail.builder()
            .itemDetailContent(requestDto.getContents())
            .build();

        // 이미지 업로드 및 연결
        List<String> imageUrls;
        try {
            imageUrls = uploadImagesAndGetUrls(images);
        } catch (IOException e) {
            throw new CommonException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        for (String url : imageUrls) {
            ItemImage itemImage = ItemImage.builder()
                .itemDetail(itemDetail)
                .url(url)
                .build();

            itemDetail.addImage(itemImage);
        }

        // 물품 생성
        Item item = Item.builder()
            .seller(user)
            .category(category)
            .tradingMethod(tradingMethod)
            .region(region)
            .itemDetail(itemDetail)
            .build();

        // 경매 진행 중 정보 생성
        AuctionProgressItem auctionProgressItem = AuctionProgressItem.builder()
            .item(item)
            .thumbnail(imageUrls.get(0))
            .itemTitle(requestDto.getTitle())
            .bidFinishTime(requestDto.getFinishTime())
            .location(region.getRegion())
            .buyNowPrice(requestDto.getBuyNowPrice())
            .startPrice(requestDto.getStartPrice())
            .maxPrice(requestDto.getStartPrice())
            .build();

        // 저장
        itemRepository.save(item);
        auctionProgressItemRepository.save(auctionProgressItem);

        // 엘라스틱 서치 저장
        itemElasticsearchRepository.save(ItemDocument.builder()
            .itemTitle(requestDto.getTitle())
            .itemId(item.getItemId())
            .createAt(item.getCreatedAt())
            .build());

        try {
            itemRecommendationService.createEmbeddingAfterItemRegister(
                item.getItemId(),
                requestDto.getTitle(),
                imageUrls.get(0),
                region.getRegion(),
                requestDto.getContents()
            );
        } catch (JsonProcessingException e) {
            throw new CommonException(ErrorCode.EMBEDDING_API_FAILED);
        } catch (CommonException e) {
            log.warn("임베딩 저장 실패: {}", e.getMessage());
        }
    }

    // S3Client에 이미지 저장 후 url 리스트 반환
    private ArrayList<String> uploadImagesAndGetUrls(List<MultipartFile> images) throws IOException {
        ArrayList<String> urls = new ArrayList<>();

        for (MultipartFile img : images) {
            String fileName = img.getOriginalFilename();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(img.getSize());
            metadata.setContentType(img.getContentType());
            amazonS3Client.putObject(bucket, fileName, img.getInputStream(), metadata);
            urls.add(amazonS3Client.getUrl(bucket, fileName).toString());
        }

        return urls;
    }
    private ItemDetail findDetailByItemId(Long itemId){
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

    private QnaComment findCommentById(long commentId) {
        Optional<QnaComment> optionalQnaComment = qnaCommentRepository.findById(commentId);
        return optionalQnaComment.orElse(null);
    }

    public ItemDetailRequestDto getItemDetail(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found"));

        AuctionProgressItem progressItem = auctionProgressItemRepository.findByItemItemId(itemId).orElse(null);

        AuctionCompleteItem completeItem = null;
        if (progressItem == null) {
            completeItem = auctionCompleteItemRepository.findByItemItemId(itemId).orElseThrow(
                () -> new RuntimeException(
                    "AuctionProgressItem or AuctionCompleteItem not found for itemId: " + itemId));
        }

        ItemDetail itemDetail = itemDetailRepository.findByItemId(itemId)
            .orElseThrow(() -> new RuntimeException("ItemDetail not found for itemId: " + itemId));

        List<ItemQuestion> itemQuestions = itemQuestionRepository.findByItemDetailId_ItemDetailId(
            itemDetail.getItemDetailId());

        return toDTO(item, progressItem, completeItem, itemDetail, itemQuestions);
    }

    // DTO 생성 메서드
    private ItemDetailRequestDto toDTO(Item item, AuctionProgressItem progressItem, AuctionCompleteItem completeItem,
        ItemDetail itemDetail, List<ItemQuestion> itemQuestions) {
        ItemDetailRequestDto dto = new ItemDetailRequestDto();
        dto.setItemId(item.getItemId());
        dto.setTitle(progressItem != null ? progressItem.getItemTitle() : completeItem.getItemTitle());
        dto.setBidFinishTime(progressItem != null ? progressItem.getBidFinishTime() : completeItem.getBidFinishTime());
        dto.setStartPrice(progressItem != null ? progressItem.getStartPrice() : completeItem.getStartPrice());
        dto.setMaxPrice((progressItem != null && progressItem.getMaxPersonNickName() == null) ? 0
            : ((progressItem != null) ? progressItem.getMaxPrice()
                : ((completeItem.getMaxPersonNickName() == null) ? 0 : completeItem.getMaxPrice())));

        // Optional을 사용하여 null 체크 및 값 설정
        Integer buyNowPrice = Optional.ofNullable(progressItem != null ? progressItem.getBuyNowPrice()
            : completeItem != null ? completeItem.getBuyNowPrice() : null).orElse(null);
        dto.setBuyNowPrice(buyNowPrice);

        dto.setAuctionComplete(item.isAuctionComplete());
        dto.setItemCreateTime(item.getCreatedAt());
        dto.setSellerId(item.getSeller().getUserId());
        dto.setUserNickname(item.getSeller().getUserNickname());
        dto.setItemDetailContent(itemDetail.getItemDetailContent());
        dto.setCategoryName(item.getCategory().getCategory());

        dto.setImages(itemDetail.getImages().stream().map(image -> {
            ItemDetailRequestDto.ImageDTO imageDTO = new ItemDetailRequestDto.ImageDTO();
            imageDTO.setImageURL(image.getUrl());
            return imageDTO;
        }).collect(Collectors.toList()));

        dto.setQuestions(itemQuestions.stream().map(question -> {
            ItemDetailRequestDto.QuestionDTO questionDTO = new ItemDetailRequestDto.QuestionDTO();
            questionDTO.setQuestionId(question.getItemQuestionId());
            questionDTO.setQuestionContents(question.getQuestionContents());
            questionDTO.setQuestionTime(question.getCreatedAt());
            questionDTO.setComments(question.getComments().stream().map(comment -> {
                ItemDetailRequestDto.QuestionDTO.CommentDTO commentDTO = new ItemDetailRequestDto.QuestionDTO.CommentDTO();
                commentDTO.setCommentId(comment.getQnaCommentId());
                commentDTO.setCommentTime(comment.getCreatedAt());
                commentDTO.setComment(comment.getComment());
                return commentDTO;
            }).collect(Collectors.toList()));
            return questionDTO;
        }).collect(Collectors.toList()));

        return dto;
    }

    // 플라스크에서 받은 아이템 list dto로 작성
    public List<RecommendDto> getItemsByIds(List<Long> itemIds) {
        // 받은 리스트 출력
        System.out.println("Received item IDs: " + itemIds);

        List<AuctionProgressItem> auctionItems = auctionProgressItemRepository.findAllById(itemIds);

        // 아이템 아이디를 키로 하는 맵을 생성
        Map<Long, AuctionProgressItem> itemMap = auctionItems.stream()
            .collect(Collectors.toMap(item -> item.getItem().getItemId(), item -> item));

        // 원래의 순서를 유지하면서 DTO 리스트 생성
        List<RecommendDto> recommendDtos = itemIds.stream().map(itemId -> {
            AuctionProgressItem item = itemMap.get(itemId);
            return new RecommendDto(item.getItem().getItemId(), item.getItemTitle(), item.getThumbnail(),
                item.getMaxPrice());
        }).collect(Collectors.toList());

        // 반환하는 리스트 출력
        System.out.println("Returning DTOs: " + recommendDtos);

        return recommendDtos;
    }


}