package com.kcs3.auction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kcs3.auction.dto.ItemEmbeddingRequestDto;
import com.kcs3.auction.dto.ItemEmbeddingResponseDto;
import com.kcs3.auction.entity.Recommend;
import com.kcs3.auction.exception.CommonException;
import com.kcs3.auction.exception.ErrorCode;
import com.kcs3.auction.repository.RecommendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    @Value("${EMBEDDING_SERVER_URL}")
    private String embeddingServerUrl;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final RecommendRepository recommendRepository;

    // 물품 임베딩 값 저장
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void storeEmbeddingAfterItemRegister(
        Long itemId,
        String title,
        String thumbnailUrl,
        String categoryName,
        String description
    ) throws JsonProcessingException {

        ItemEmbeddingRequestDto embeddingRequestDto = ItemEmbeddingRequestDto.builder()
            .itemId(itemId)
            .title(title)
            .thumbnailUrl(thumbnailUrl)
            .categoryName(categoryName)
            .description(description)
            .build();

        // 임베딩 서버 API 호출
        ItemEmbeddingResponseDto embeddingResponseDto = webClient.post()
            .uri(embeddingServerUrl + "/api/getEmbeddingVec")
            .bodyValue(embeddingRequestDto)
            .retrieve()
            .bodyToMono(ItemEmbeddingResponseDto.class)
            .block();

        Recommend recommend = Recommend.builder()
            .itemId(itemId)
            .titleEmbeddingVec(objectMapper.writeValueAsString(embeddingResponseDto.getTitleEmbedding()))
            .thumbnailEmbeddingVec(objectMapper.writeValueAsString(embeddingResponseDto.getThumbnailEmbedding()))
            .categoryEmbeddingVec(objectMapper.writeValueAsString(embeddingResponseDto.getCategoryEmbedding()))
            .detailEmbeddingVec(objectMapper.writeValueAsString(embeddingResponseDto.getDescriptionEmbedding()))
            .build();

        // 3. 임베딩 DB 저장
        recommendRepository.save(recommend);

        // 대표 임베딩 생성 API 호출
        ResponseEntity<String> result = webClient.get()
            .uri(embeddingServerUrl + "/api/getRepresentEmbeddingVec")
            .retrieve()
            .toEntity(String.class)
            .block();

        if (result == null || !result.getStatusCode().is2xxSuccessful()) {
            throw new CommonException(ErrorCode.EMBEDDING_SAVE_FAILED);
        }

        // 대표 임베딩 DB 저장
        recommend.setRepresentEmbeddingVec(result.getBody());
        recommendRepository.save(recommend);
    }
}
