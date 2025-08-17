 package com.kcs3.auction.service;

 import co.elastic.clients.elasticsearch.ElasticsearchClient;
 import co.elastic.clients.elasticsearch._types.ElasticsearchException;
 import co.elastic.clients.elasticsearch._types.Retriever;
 import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
 import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
 import co.elastic.clients.elasticsearch._types.query_dsl.Query;
 import co.elastic.clients.elasticsearch.core.SearchRequest;
 import co.elastic.clients.elasticsearch.core.SearchResponse;
 import com.kcs3.auction.document.ItemSearchDocument;
 import com.kcs3.auction.dto.ItemPreviewDto;
 import com.kcs3.auction.dto.SearchFilterDto;
 import com.kcs3.auction.exception.CommonException;
 import com.kcs3.auction.exception.ErrorCode;
 import com.kcs3.auction.repository.CategoryRepository;
 import com.kcs3.auction.repository.ItemRepository;
 import com.kcs3.auction.repository.RegionRepository;
 import com.kcs3.auction.repository.TradingMethodRepository;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.stream.Collectors;
 import lombok.RequiredArgsConstructor;
 import lombok.extern.log4j.Log4j2;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Pageable;
 import org.springframework.data.domain.Slice;
 import org.springframework.data.domain.SliceImpl;
 import org.springframework.data.domain.Sort;
 import org.springframework.data.elasticsearch.client.elc.NativeQuery;
 import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
 import org.springframework.data.elasticsearch.core.SearchHits;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.reactive.function.client.WebClientResponseException;

@Log4j2
@Service
@RequiredArgsConstructor
public class ItemSearchService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final TradingMethodRepository tradingMethodRepository;
    private final RegionRepository regionRepository;

    private final SearchEmbeddingService searchEmbeddingService;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchClient elasticsearchClient;

    // 조건별 물품 목록 조회
    @Transactional(readOnly = true)
    public Slice<ItemPreviewDto> searchItemList(
        String keyword,
        String categoryName,
        Integer tradingMethodCode,
        String regionName,
        Boolean status,
        Pageable pageable
    ) {
        Long categoryId = null;
        if (categoryName != null) {
            categoryId = getCategoryIdOrThrow(categoryName);
        }

        Long tradingMethodId = null;
        if (tradingMethodCode != null) {
            tradingMethodId = getTradingMethodIdOrThrow(tradingMethodCode);
        }

        Long regionId = null;
        if (regionName != null) {
            regionId = getRegionIdOrThrow(regionName);
        }

        if (keyword == null || keyword.isBlank()) {
            return itemRepository.fetchItemPreviewsByFilters(
                null,        // itemIdList
                null,              // sellerId
                categoryId,        // categoryId
                tradingMethodId,   // tradingMethodId
                regionId,          // regionId
                status,            // isAuctionComplete
                pageable
            );
        }

        List<Long> ids = null;
        SearchFilterDto filters = SearchFilterDto.of(categoryId, regionId, tradingMethodId, status);

        try {
            float[] queryEmbedding = searchEmbeddingService.createEmbedding(keyword);
            ids = searchItemsWithVector(queryEmbedding, keyword, filters, pageable);
            return itemRepository.fetchItemPreviewsByItemIdOrder(ids, status, pageable);
        } catch (WebClientResponseException e) {
            log.error("OpenAI 임베딩 생성 실패: keyword={}, status={}, error={}", keyword, e.getStatusCode(), e.getMessage());
        } catch (ElasticsearchException | IOException e) {
            log.error("Elasticsearch 벡터 검색 실패: keyword={}, message={}", keyword, e.getMessage());
        }

        // fallback: 벡터 검색 실패 시 키워드 기반 검색 수행
        // - OpenAI API 오류 또는 Elasticsearch kNN 오류 대응
        try {
            ids = searchItemsKeywordOnly(keyword, filters, pageable);
            return itemRepository.fetchItemPreviewsByItemIdOrder(ids, status, pageable);
        } catch (ElasticsearchException e) {
            log.error("Elasticsearch 키워드 검색 실패: keyword={}, message={}", keyword, e.getMessage());
        }

        // fallback: 키워드 기반 Like 검색 수행
        // - LIKE '%키워드%' 검색은 인덱스를 활용할 수 없어 전체 테이블 스캔 발생
        // - COALESCE + LIKE 조합은 모든 레코드에 대해 계산이 필요
        // - 따라서) itemId 추출 -> 상세 정보 조회
        ids = itemRepository.findItemIdsByKeyword(keyword, pageable);

        if (ids.isEmpty()) {
            return new SliceImpl<>(Collections.emptyList(), pageable, false);
        }

        return itemRepository.fetchItemPreviewsByFilters(
            ids,        // itemIdList
            null,              // sellerId
            categoryId,        // categoryId
            tradingMethodId,   // tradingMethodId
            regionId,          // regionId
            status,            // isAuctionComplete
            pageable
        );
    }

    private Long getCategoryIdOrThrow(String categoryName) {
        Long id = categoryRepository.findIdByCategoryName(categoryName);
        if (id == null) {
            throw new CommonException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        return id;
    }

    private Long getTradingMethodIdOrThrow(Integer tradingMethodCode) {
        Long id = tradingMethodRepository.findIdByTmCode(tradingMethodCode);
        if (id == null) {
            throw new CommonException(ErrorCode.TRADING_METHOD_NOT_FOUND);
        }
        return id;
    }

    private Long getRegionIdOrThrow(String regionName) {
        Long id = regionRepository.findIdByRegionName(regionName);
        if (id == null) {
            throw new CommonException(ErrorCode.DEFAULT_REGION_NOT_FOUND);
        }
        return id;
    }

    // Elasticsearch(벡터 + 키워드) 검색
    private List<Long> searchItemsWithVector(
        float[] queryEmbedding,
        String keyword,
        SearchFilterDto filters,
        Pageable pageable
    ) throws IOException {
        List<Float> queryVectorList = new ArrayList<>(queryEmbedding.length);
        for (float v : queryEmbedding) {
            queryVectorList.add(v);
        }

        List<Query> filterQueries = createVectorSearchFilterQueries(filters);

        SearchRequest searchRequest = SearchRequest.of(s -> s
            .index("items")
            .size(pageable.getPageSize())
            .retriever(r -> r
                .rrf(rrf -> rrf
                    .rankWindowSize(100)
                    .rankConstant(60)
                    .retrievers(List.of(
                        // 벡터 검색 retriever
                        Retriever.of(rv -> rv.knn(k -> k
                            .field("embedding")
                            .queryVector(queryVectorList)
                            .k(50)
                            .numCandidates(100)
                            .filter(filterQueries)
                        )),
                        // 키워드 검색 retriever
                        Retriever.of(rv -> rv.standard(st -> st
                            .query(q -> q
                                .bool(b -> b
                                    .filter(filterQueries)
                                    .must(m -> m
                                        .multiMatch(mm -> mm
                                            .fields(List.of("itemTitle^2.0", "searchText"))
                                            .query(keyword)
                                            .fuzziness("AUTO")
                                        )
                                    )
                                )
                            )
                        ))
                    ))
                )
            )
        );

        SearchResponse<ItemSearchDocument> response =
            elasticsearchClient.search(searchRequest, ItemSearchDocument.class);

        return response.hits().hits().stream()
            .map(hit -> hit.source().getItemId())
            .collect(Collectors.toList());
    }

    // 벡터 검색용 필터 생성
    private List<Query> createVectorSearchFilterQueries(SearchFilterDto filters) {
        List<Query> queries = new ArrayList<>();

        if (filters.categoryId() != null) {
            queries.add(Query.of(q -> q
                .term(t -> t.field("categoryId").value(filters.categoryId()))
            ));
        }
        if (filters.regionId() != null) {
            queries.add(Query.of(q -> q
                .term(t -> t.field("regionId").value(filters.regionId()))
            ));
        }
        if (filters.tradingMethodId() != null) {
            queries.add(Query.of(q -> q
                .term(t -> t.field("tradingMethodId").value(filters.tradingMethodId()))
            ));
        }
        if (filters.isAuctionComplete() != null) {
            queries.add(Query.of(q -> q
                .term(t -> t.field("isAuctionComplete").value(filters.isAuctionComplete()))
            ));
        }

        return queries;
    }

    // Elasticsearch(키워드) 검색
    private List<Long> searchItemsKeywordOnly(String keyword, SearchFilterDto filters, Pageable pageable) {
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        // 키워드 검색 (multi_match)
        boolBuilder.must(m -> m
            .multiMatch(mm -> mm
                .fields(List.of("itemTitle", "searchText"))
                .query(keyword)
                .fuzziness("AUTO")
                .operator(Operator.And)
            )
        );

        // 조건부 필터 추가
        applyKeywordSearchFilters(boolBuilder, filters);

        NativeQuery query = NativeQuery.builder()
            .withQuery(boolBuilder.build()._toQuery())
            .withSort(Sort.by(Sort.Direction.DESC, "_score", "createdAt")) // 관련도 -> 최신순
            .withPageable(PageRequest.of(0, pageable.getPageSize() * 2))
            .build();

        SearchHits<ItemSearchDocument> searchHits = elasticsearchOperations.search(query, ItemSearchDocument.class);

        return searchHits.getSearchHits().stream()
            .map(hit -> hit.getContent().getItemId())
            .collect(Collectors.toList());
    }

    // 키워드 검색용 필터 생성
    private void applyKeywordSearchFilters(BoolQuery.Builder boolBuilder, SearchFilterDto filters) {
        if (filters.categoryId() != null) {
            boolBuilder.filter(f -> f.term(t -> t.field("categoryId").value(filters.categoryId())));
        }
        if (filters.regionId() != null) {
            boolBuilder.filter(f -> f.term(t -> t.field("regionId").value(filters.regionId())));
        }
        if (filters.tradingMethodId() != null) {
            boolBuilder.filter(f -> f.term(t -> t.field("tradingMethodId").value(filters.tradingMethodId())));
        }
        if (filters.isAuctionComplete() != null) {
            boolBuilder.filter(f -> f.term(t -> t.field("isAuctionComplete").value(filters.isAuctionComplete())));
        }
    }
}
