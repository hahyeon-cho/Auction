package com.kcs3.auction.service;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.kcs3.auction.document.ItemDocument;
import com.kcs3.auction.dto.AuctionSummaryDto;
import com.kcs3.auction.dto.ImageDto;
import com.kcs3.auction.dto.ItemCreateRequestDto;
import com.kcs3.auction.dto.ItemDetailResponseDto;
import com.kcs3.auction.dto.QuestionWithAnswerDto;
import com.kcs3.auction.entity.AuctionCompleteItem;
import com.kcs3.auction.entity.AuctionProgressItem;
import com.kcs3.auction.entity.Category;
import com.kcs3.auction.entity.Item;
import com.kcs3.auction.entity.ItemDetail;
import com.kcs3.auction.entity.ItemImage;
import com.kcs3.auction.entity.ItemQuestion;
import com.kcs3.auction.entity.Region;
import com.kcs3.auction.entity.TradingMethod;
import com.kcs3.auction.entity.User;
import com.kcs3.auction.exception.CommonException;
import com.kcs3.auction.exception.ErrorCode;
import com.kcs3.auction.repository.AuctionCompleteItemRepository;
import com.kcs3.auction.repository.AuctionProgressItemRepository;
import com.kcs3.auction.repository.CategoryRepository;
import com.kcs3.auction.repository.ItemElasticsearchRepository;
import com.kcs3.auction.repository.ItemRepository;
import com.kcs3.auction.repository.RegionRepository;
import com.kcs3.auction.repository.TradingMethodRepository;
import com.kcs3.auction.utils.AuthUserProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    @Value("${aws.s3.bucket-name}")
    private String s3Bucket;

    private final AmazonS3 amazonS3Client = AmazonS3ClientBuilder.standard()
        .withRegion(Regions.AP_NORTHEAST_2)
        .build();

    private final AuthUserProvider authUserProvider;

    private final ItemRepository itemRepository;
    private final AuctionProgressItemRepository auctionProgressItemRepository;
    private final AuctionCompleteItemRepository auctionCompleteItemRepository;
    private final CategoryRepository categoryRepository;
    private final TradingMethodRepository tradingMethodRepository;
    private final RegionRepository regionRepository;

    private final ItemQuestionService questionService;

    private final SearchEmbeddingService searchEmbeddingService;
    private final ItemElasticsearchRepository itemElasticsearchRepository;
    private final RecommendService itemRecommendationService;

    // === 물품 등록 서비스 ===
    @Transactional
    public void createAuctionItem(ItemCreateRequestDto requestDto, List<MultipartFile> images) {
        User user = authUserProvider.getCurrentUser();

        Category category = categoryRepository.findByCategory(requestDto.getCategory())
            .orElseThrow(() -> new CommonException(ErrorCode.CATEGORY_NOT_FOUND));

        TradingMethod tradingMethod = tradingMethodRepository.findByTradingMethod(requestDto.getTradingMethod())
            .orElseThrow(() -> new CommonException(ErrorCode.TRADING_METHOD_NOT_FOUND));

        Region region = regionRepository.findByRegionName(requestDto.getRegion())
            .orElseGet(() ->
                regionRepository.findByRegionName("기타")
                    .orElseThrow(() -> new CommonException(ErrorCode.DEFAULT_REGION_NOT_FOUND))
            );

        // 상세 정보 생성
        ItemDetail itemDetail = ItemDetail.builder()
            .itemDetailContent(requestDto.getContents())
            .build();

        // 이미지 업로드 및 연결
        List<String> imageUrls;
        try {
            imageUrls = uploadImagesAndGetUrls(images);
        } catch (IOException e) {
            throw new CommonException(ErrorCode.FILE_UPLOAD_FAILED);
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
            .itemTitle(requestDto.getTitle())
            .thumbnail(imageUrls.get(0))
            .location(region.getRegionName())
            .bidFinishTime(requestDto.getFinishTime())
            .buyNowPrice(requestDto.getBuyNowPrice())
            .startPrice(requestDto.getStartPrice())
            .maxPrice(requestDto.getStartPrice())
            .build();

        // 저장
        itemRepository.save(item);
        auctionProgressItemRepository.save(auctionProgressItem);

        // 엘라스틱 서치 저장
        try {
            ItemDocument itemDocument = buildItemDocument(requestDto, item, category, region, tradingMethod);
            itemElasticsearchRepository.save(itemDocument);
        } catch (Exception e) {
            throw new CommonException(ErrorCode.ITEM_ELASTIC_SAVE_FAILED);
        }

        // 추천 서비스 임베딩 값 저장
        try {
            itemRecommendationService.storeEmbeddingAfterItemRegister(
                item,
                requestDto.getTitle(),
                imageUrls.get(0),
                region.getRegionName(),
                requestDto.getContents()
            );
        } catch (JsonProcessingException e) {
            throw new CommonException(ErrorCode.RECOMMEND_EMBEDDING_SAVE_FAILED);
        } catch (Exception e) {
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

            amazonS3Client.putObject(s3Bucket, fileName, img.getInputStream(), metadata);
            String imageUrl = amazonS3Client.getUrl(s3Bucket, fileName).toString();
            urls.add(imageUrl);
        }

        return urls;
    }

    private ItemDocument buildItemDocument(
        ItemCreateRequestDto requestDto,
        Item item,
        Category category,
        Region region,
        TradingMethod tradingMethod
    ) {
        String searchText = String.join(" ",
            requestDto.getTitle(),
            category.getCategoryName(),
            requestDto.getContents() != null ? requestDto.getContents() : ""
        ).trim();

        // 임베딩 생성
        try {
            float[] embedding = searchEmbeddingService.createEmbedding(searchText);

            return ItemDocument.builder()
                .itemId(item.getItemId())
                .itemTitle(requestDto.getTitle())
                .categoryId(category.getCategoryId())
                .regionId(region.getRegionId())
                .tradingMethodId(tradingMethod.getTradingMethodId())
                .isAuctionComplete(false)
                .createdAt(item.getCreatedAt())
                .searchText(searchText)
                .embedding(embedding)
                .build();
        } catch (Exception e) {
            log.error("검색용 임베딩 생성 실패 - 상품ID: {}, 텍스트: {}", item.getItemId(), searchText, e);
            throw new CommonException(ErrorCode.SEARCH_EMBEDDING_FAILED);
        }
    }

    // === 물품 상세 정보 조회 ===
    @Transactional(readOnly = true)
    public ItemDetailResponseDto loadItemDetail(Long itemId) {
        // --- 객체 호출 ---
        Item item = itemRepository.findById(itemId)
            .orElseThrow(() -> new CommonException(ErrorCode.ITEM_NOT_FOUND));

        User seller = item.getSeller();
        ItemDetail itemDetail = item.getItemDetail();
        List<ItemQuestion> itemQuestions = itemDetail.getQnas();

        // --- 매핑 ---
        // 물품 경매 관련 정보 매핑 (경매 중 테이블 or 경매 완료 테이블)
        AuctionSummaryDto auctionSummaryDto = loadAuctionSummary(item.getItemId(), item.isAuctionComplete());

        // 이미지 url 정보 Dto 매핑
        List<ImageDto> imageDtos = itemDetail.getImages().stream()
            .map(image -> ImageDto.builder()
                .imageUrl(image.getUrl())
                .build())
            .collect(Collectors.toList());

        // 문의글 및 문의 답글 Dto 매핑
        List<QuestionWithAnswerDto> qnaDtos = questionService.convertToQnaDtos(itemDetail.getQnas());

        ItemDetailResponseDto responseDto = ItemDetailResponseDto.builder()
            .itemId(item.getItemId())
            .isAuctionComplete(item.isAuctionComplete())
            .itemCreatedAt(item.getCreatedAt())
            .sellerId(seller.getUserId())
            .nickname(seller.getNickname())
            .categoryName(item.getCategory().getCategoryName())
            .tmCode(item.getTradingMethod().getTmCode())
            .location(auctionSummaryDto.location())
            .itemTitle(auctionSummaryDto.itemTitle())
            .startPrice(auctionSummaryDto.startPrice())
            .buyNowPrice(auctionSummaryDto.buyNowPrice())
            .maxPrice(auctionSummaryDto.maxPrice())
            .bidFinishTime(auctionSummaryDto.bidFinishTime())
            .itemDetailContent(itemDetail.getItemDetailContent())
            .images(imageDtos)
            .questions(qnaDtos)
            .build();

        return responseDto;
    }

    private AuctionSummaryDto loadAuctionSummary(Long itemId, boolean isComplete) {
        if (!isComplete) {
            AuctionProgressItem progress = auctionProgressItemRepository.findByItemItemId(itemId)
                .orElseThrow(() -> new CommonException(ErrorCode.ITEM_NOT_FOUND));
            return AuctionSummaryDto.from(progress);
        } else {
            AuctionCompleteItem complete = auctionCompleteItemRepository.findByItemItemId(itemId)
                .orElseThrow(() -> new CommonException(ErrorCode.ITEM_NOT_FOUND));
            return AuctionSummaryDto.from(complete);
        }
    }
}
