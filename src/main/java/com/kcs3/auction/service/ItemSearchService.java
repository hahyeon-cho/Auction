 package com.kcs3.auction.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import com.kcs3.auction.document.ItemSearchDocument;
import com.kcs3.auction.dto.ItemPreviewDto;
import com.kcs3.auction.exception.CommonException;
import com.kcs3.auction.exception.ErrorCode;
import com.kcs3.auction.repository.CategoryRepository;
import com.kcs3.auction.repository.ItemRepository;
import com.kcs3.auction.repository.RegionRepository;
import com.kcs3.auction.repository.TradingMethodRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemSearchService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final TradingMethodRepository tradingMethodRepository;
    private final RegionRepository regionRepository;

    private final ElasticsearchOperations elasticsearchOperations;

    // 조건별 아이템 목록 조회
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

        List<Long> itemIdList = (keyword != null && !keyword.isBlank())
            ? searchItemsByKeyword(keyword, pageable)
            : null;

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

    // 엘라스틱 서치에 접근하여 키워드 검색
    private List<Long> searchItemsByKeyword(String keyword, Pageable pageable) {
        Query query = NativeQuery.builder()
            .withQuery(q -> q
                .multiMatch(m -> m
                    .fields(List.of("itemTitle"))
                    .query(keyword)
                    .fuzziness("AUTO") // 모호한 검색 허용
                    .operator(Operator.And) // 검색 단어가 모두 포함
                )
            )
            .withSort(Sort.by(Sort.Direction.DESC, "createdAt"))
            .withPageable(pageable)
            .build();

        SearchHits<ItemSearchDocument> searchHits = elasticsearchOperations.search(query,
            ItemSearchDocument.class);

        List<Long> itemIds = searchHits.getSearchHits().stream()
            .map(hit -> hit.getContent().itemId())
            .toList();

        return itemIds;
    }
}
