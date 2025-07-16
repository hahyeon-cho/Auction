package com.kcs3.auction.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchEmbeddingService {

    @Value("${openai.embedding.dimensions:1536}")
    private int dimensions;

    @Qualifier("openAiWebClient")
    private final WebClient webClient;

    public float[] createEmbedding(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Embedding text must not be blank");
        }

        Map<String, Object> requestBody = Map.of(
            "model", "text-embedding-3-small",  // small도 1536 지원
            "input", text,
            "dimensions", dimensions
        );

        Map<String, Object> response = webClient
            .post()
            .uri("/embeddings")
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Map.class)
            .block();

        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        List<Double> embedding = (List<Double>) data.get(0).get("embedding");

        float[] result = new float[embedding.size()];
        for (int i = 0; i < embedding.size(); i++) {
            result[i] = embedding.get(i).floatValue();
        }
        return result;
    }
}