package com.kcs3.auction.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import com.kcs3.auction.document.ItemSearchDocument;
import com.kcs3.auction.dto.ItemPreviewDto;
import com.kcs3.auction.repository.ItemRepository;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ExtendWith(MockitoExtension.class)
@DisplayName("ItemSearchService 폴백 로직 테스트")
class ItemSearchServiceFallbackTest {

    @Mock private ItemRepository itemRepository;
    @Mock private SearchEmbeddingService searchEmbeddingService;
    @Mock private ElasticsearchOperations elasticsearchOperations;

    @InjectMocks
    private ItemSearchService itemSearchService;

    private Pageable pageable;
    private Slice<ItemPreviewDto> mockSlice;
    private List<Long> mockItemIds;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);
        mockSlice = new SliceImpl<>(Collections.emptyList(), pageable, false);
        mockItemIds = List.of(1L, 2L, 3L);
    }

    @Test
    @DisplayName("keyword가 null → 벡터 검색 x → RDBMS 필터 기반 조회")
    void skipVectorSearchWhenKeywordIsNull() {
        given(itemRepository.fetchItemPreviewsByFilters(
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            eq(pageable)
        )).willReturn(mockSlice);

        Slice<ItemPreviewDto> result =
            itemSearchService.searchItemList(
                null, null, null, null, null, pageable
            );

        assertThat(result).isNotNull();

        verify(searchEmbeddingService, never())
            .createEmbedding(anyString());
    }

    @Test
    @DisplayName("OpenAI 임베딩 생성 실패 → Elasticsearch 키워드 검색으로 폴백")
    void fallbackToKeywordSearchWhenEmbeddingFails() {
        String keyword = "노트북";

        WebClientResponseException mockException =
            WebClientResponseException.create(
                500,
                "Internal Server Error",
                null,
                null,
                null
            );

        given(searchEmbeddingService.createEmbedding(keyword))
            .willThrow(mockException);

        SearchHits<ItemSearchDocument> mockHits = mock(SearchHits.class);

        given(elasticsearchOperations.search(
            any(Query.class),
            eq(ItemSearchDocument.class)
        )).willReturn(mockHits);

        given(mockHits.getSearchHits())
            .willReturn(List.<SearchHit<ItemSearchDocument>>of());

        given(itemRepository.fetchItemPreviewsByItemIdOrder(
            anyList(),
            isNull(),
            eq(pageable)
        )).willReturn(mockSlice);

        Slice<ItemPreviewDto> result =
            itemSearchService.searchItemList(
                keyword, null, null, null, null, pageable
            );

        assertThat(result).isNotNull();

        verify(itemRepository, never())
            .findItemIdsByKeyword(any(), any());
    }

    @Test
    @DisplayName("Elasticsearch 키워드 검색 실패 → RDBMS LIKE 검색으로 폴백")
    void fallbackToLikeSearchWhenKeywordSearchFails() {
        String keyword = "노트북";

        WebClientResponseException mockException =
            WebClientResponseException.create(
                500,
                "Internal Server Error",
                null,
                null,
                null
            );

        given(searchEmbeddingService.createEmbedding(keyword))
            .willThrow(mockException);

        given(elasticsearchOperations.search(
            any(Query.class),
            eq(ItemSearchDocument.class)
        )).willThrow(ElasticsearchException.class);

        given(itemRepository.findItemIdsByKeyword(keyword, pageable))
            .willReturn(mockItemIds);

        given(itemRepository.fetchItemPreviewsByFilters(
            eq(mockItemIds),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            eq(pageable)
        )).willReturn(mockSlice);

        Slice<ItemPreviewDto> result =
            itemSearchService.searchItemList(
                keyword, null, null, null, null, pageable
            );

        assertThat(result).isNotNull();

        verify(itemRepository)
            .findItemIdsByKeyword(keyword, pageable);
    }
}
