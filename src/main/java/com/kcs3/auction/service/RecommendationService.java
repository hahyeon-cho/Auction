package com.kcs3.auction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcs3.auction.dto.ItemEmbeddingRequestDto;
import com.kcs3.auction.dto.ItemEmbeddingResponseDto;
import com.kcs3.auction.dto.RecommendDto;
import com.kcs3.auction.entity.AuctionProgressItem;
import com.kcs3.auction.entity.Item;
import com.kcs3.auction.entity.Recommend;
import com.kcs3.auction.repository.AuctionProgressItemRepository;
import com.kcs3.auction.repository.ItemRepository;
import com.kcs3.auction.repository.RecommendRepository;
import jakarta.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    @Value("${PYTHON_SERVER_URL}")
    private String pythonServerUrl;

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    private final ItemRepository itemRepository;
    private final RecommendRepository recommendRepository;
    private final AuctionProgressItemRepository auctionRepository;

    private WebClient webClient;

    @PostConstruct
    private void initWebClient() {
        this.webClient = webClientBuilder.baseUrl(pythonServerUrl).build();
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void storeEmbeddingAfterItemRegister(
        Long itemId,
        String title,
        String thumbnailUrl,
        String categoryName,
        String description
    ) throws JsonProcessingException {

        // 1) 파이썬 서버에 임베딩 생성 요청
        ItemEmbeddingRequestDto embeddingRequest = ItemEmbeddingRequestDto.builder()
            .itemId(itemId)
            .title(title)
            .thumbnailUrl(thumbnailUrl)
            .categoryName(categoryName)
            .description(description)
            .build();

        ItemEmbeddingResponseDto embeddingResponse = webClient.post()
            .uri("/api/getEmbeddingVec")
            .bodyValue(embeddingRequest)
            .retrieve()
            .bodyToMono(ItemEmbeddingResponseDto.class)
            .block();

        // 2) 임베딩 DB 저장
        Item itemRef = itemRepository.getReferenceById(itemId);

        Recommend recommend = Recommend.builder()
            .item(itemRef)
            .titleEmbeddingVec(objectMapper.writeValueAsString(embeddingResponse.getTitleEmbedding()))
            .thumbnailEmbeddingVec(objectMapper.writeValueAsString(embeddingResponse.getThumbnailEmbedding()))
            .categoryEmbeddingVec(objectMapper.writeValueAsString(embeddingResponse.getCategoryEmbedding()))
            .detailEmbeddingVec(objectMapper.writeValueAsString(embeddingResponse.getDescriptionEmbedding()))
            .build();

        recommendRepository.save(recommend);

        // 3) 대표 임베딩 생성 요청
        String representEmbedding = webClient.get()
            .uri("/api/getRepresentEmbeddingVec")
            .retrieve()
            .bodyToMono(String.class)
            .block();

        // 4) 대표 임베딩 업데이트
        recommend.updateRepresentEmbeddingVec(representEmbedding);
        recommendRepository.save(recommend);
    }

    // 유사 아이템 추천
    @Transactional(readOnly = true)
    public List<RecommendDto> getSimilarItems(Long itemId) {

        // 파이썬 추천 서버 호출
        List<Long> similarIds = webClient.post()
            .uri("/api/Recommend")
            .bodyValue(Map.of("itemId", itemId))
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<Long>>() {})
            .block();

        if (similarIds == null || similarIds.isEmpty()) {
            return Collections.emptyList();
        }

        // DB 조회
        List<AuctionProgressItem> items = auctionRepository.findAllById(similarIds);

        Map<Long, AuctionProgressItem> itemMap = items.stream()
            .collect(Collectors.toMap(
                ap -> ap.getItem().getItemId(),
                ap -> ap
            ));

        // 파이썬 서버에서 준 물품 순서대로 Dto 생성
        return similarIds.stream()
            .map(id -> {
                AuctionProgressItem item = itemMap.get(id);

                if (item == null) {
                    return RecommendDto.builder().itemId(id).build();
                }

                return RecommendDto.builder()
                    .itemId(item.getItem().getItemId())
                    .itemTitle(item.getItemTitle())
                    .thumbnail(item.getThumbnail())
                    .maxPrice(item.getMaxPrice())
                    .build();
            })
            .collect(Collectors.toList());
    }
}