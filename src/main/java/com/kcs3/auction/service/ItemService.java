package com.kcs3.auction.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcs3.auction.document.ItemDocument;
import com.kcs3.auction.dto.CommentRequest;
import com.kcs3.auction.dto.ItemDetailRequestDto;
import com.kcs3.auction.dto.ItemEmbeddingRequestDto;
import com.kcs3.auction.dto.ItemEmbeddingResponseDto;
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
import com.kcs3.auction.entity.Recommend;
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
import com.kcs3.auction.repository.ItemImageRepository;
import com.kcs3.auction.repository.ItemQuestionRepository;
import com.kcs3.auction.repository.ItemRepository;
import com.kcs3.auction.repository.QnaCommentRepository;
import com.kcs3.auction.repository.RecommendRepository;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class ItemService {

    @Value("${EMBEDDING_SERVER_URL}")
    private String embeddingServerUrl;

    private final AuthUserProvider authUserProvider;

    private final AmazonS3 amazonS3Client = AmazonS3ClientBuilder.standard().withRegion(Regions.AP_NORTHEAST_2) // 서울 리전
        .build();

    private final UserRepository userRepository;

    private final ItemRepository itemRepository;
    private final ItemDetailRepository itemDetailRepository;

    private final ItemQuestionRepository itemQuestionRepository;
    private final QnaCommentRepository qnaCommentRepository;

    private final AuctionProgressItemRepository auctionProgressItemRepository;
    private final AuctionCompleteItemRepository auctionCompleteItemRepository;


    private final AlarmRepository alarmRepository;

    private final ItemElasticsearchRepository itemElasticsearchRepository;
    private final ObjectMapper objectMapper;
    @Value("${cloud.aws.s3.bucketName}")
    private String bucket;

    private final CategoryRepository categoryRepository;
    private final TradingMethodRepository tradingMethodRepository;
    private final RegionRepository regionRepository;

    private ItemImageRepository itemImageRepository;

    private RecommendRepository recommendRepository;

    private final WebClient webClient;


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







    //물품저장 서비스
    public void postItem(AuctionItemRequest request) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        User user = userRepository.findByUserId(customOAuth2User.getUserId())
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));

        Region region = regionRepository.findByRegion(request.region);
        if (region == null) {
            region = regionRepository.findByRegion("기타");
        }

        Item item = new Item();
        item.setCategory(request.category);
        item.setTradingMethod(request.trading_method);
        item.setAuctionComplete(false);
        item.setSeller(user);
        item.setRegion(region);

        AuctionProgressItem auctionProgressItem = new AuctionProgressItem();
        auctionProgressItem.setItemTitle(request.title);
        auctionProgressItem.setBidFinishTime(request.finish_time);
        auctionProgressItem.setStartPrice(request.start_price);

        // Null 체크 후 설정
        if (request.buy_now_price != null) {
            auctionProgressItem.setBuyNowPrice(request.buy_now_price);
        } else {
            auctionProgressItem.setBuyNowPrice(null); // null 값 설정
        }

        auctionProgressItem.setLocation("전체");
        auctionProgressItem.setItem(item);
        auctionProgressItem.setMaxPrice(request.start_price);

        ArrayList<String> imageUrls = this.saveFiles(request.images);
        if (!imageUrls.isEmpty()) {
            auctionProgressItem.setThumbnail(imageUrls.get(0));
        } else {
            throw new IOException("이미지가 제공되지 않았습니다.");
        }

        ItemDetail itemDetail = new ItemDetail();
        itemDetail.setItem(item);
        itemDetail.setItemDetailContent(request.contents);

        List<ItemImage> itemImages = new ArrayList<>();
        for (String url : imageUrls) {
            ItemImage itemImage = new ItemImage();
            itemImage.setUrl(url);
            itemImage.setItemDetail(itemDetail);
            itemImages.add(itemImage);
        }

        itemDetail.setImages(itemImages);

        itemRepository.save(item);
        auctionProgressItemRepository.save(auctionProgressItem);
        itemDetailRepository.save(itemDetail);
        for (ItemImage itemImage : itemImages) {
            itemImageRepository.save(itemImage);
        }

        //엘라스틱 저장 로직
        itemElasticsearchRepository.save(ItemDocument.builder()
                .itemTitle(auctionProgressItem.getItemTitle())
                .itemId(item.getItemId())
                .createAt(item.getCreatedAt())
                .build());
    }

    // 임베딩 값 저장 서비스 메서드 수정
    public Long getLastItemId() {
        return itemRepository.findTopByOrderByItemIdDesc().getItemId();
    }

    public void updateEmbedding(Long itemId, double[] embedding, double[] thEmbedding, double[] categoryEmbedding, double[] detailEmbedding) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid item ID: " + itemId));
        try {
            String embeddingJson = objectMapper.writeValueAsString(embedding);
            String thEmbeddingJson = objectMapper.writeValueAsString(thEmbedding);
            String categoryEmbeddingJson = objectMapper.writeValueAsString(categoryEmbedding);
            String detailEmbeddingJson = objectMapper.writeValueAsString(detailEmbedding);

            Recommend recommend = recommendRepository.findByItemId(itemId);
            if (recommend == null) {
                recommend = new Recommend();
                recommend.setItemId(itemId);
                recommend.setEmbedding(embeddingJson);
                recommend.setThEmbedding(thEmbeddingJson);
                recommend.setCategoryEmbedding(categoryEmbeddingJson);
                recommend.setDetailEmbedding(detailEmbeddingJson);
                recommendRepository.save(recommend);
            } else {
                recommend.setEmbedding(embeddingJson);
                recommend.setThEmbedding(thEmbeddingJson);
                recommend.setCategoryEmbedding(categoryEmbeddingJson);
                recommend.setDetailEmbedding(detailEmbeddingJson);
                recommendRepository.save(recommend);
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize embedding", e);
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