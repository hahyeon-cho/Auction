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

@Service
@RequiredArgsConstructor
public class ItemSearchService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final TradingMethodRepository tradingMethodRepository;
    private final RegionRepository regionRepository;

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
            categoryId = categoryRepository.findIdByCategory(categoryName);
            if (categoryId == null) {
                throw new CommonException(ErrorCode.CATEGORY_NOT_FOUND);
            }
        }

        Long tradingMethodId = null;
        if (tradingMethodCode != null) {
            tradingMethodId = tradingMethodRepository.findIdByTradingMethod(tradingMethodCode);
            if (tradingMethodId == null) {
                throw new CommonException(ErrorCode.TRADING_METHOD_NOT_FOUND);
            }
        }

        Long regionId = null;
        if (regionName != null) {
            regionId = regionRepository.findIdByRegionName(regionName);
            if (regionId == null) {
                throw new CommonException(ErrorCode.DEFAULT_REGION_NOT_FOUND);
            }
        }

        List<Long> itemIdList = null;

        return itemRepository.fetchItemPreviewsByFilters(
            itemIdList,        // itemIdList
            null,              // sellerId
            categoryId,        // categoryId
            tradingMethodId,   // tradingMethodId
            regionId,          // regionId
            status,            // isAuctionComplete
            pageable
        );
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
