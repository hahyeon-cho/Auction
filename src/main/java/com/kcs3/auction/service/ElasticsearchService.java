package com.kcs3.auction.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import com.kcs3.auction.document.ItemDocument;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class ElasticsearchService {
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    public Optional<List<ItemDocument>> searchItems(String keyword) {
        Query query = NativeQuery.builder()
                .withQuery(q -> q
                        .multiMatch(m -> m
                                .fields(List.of("itemTitle"))
                                .query(keyword)
                                .fuzziness("AUTO") //모호한 검색 허용
                                .operator(Operator.And) //검색 단어가 모두 포함
                        )
                )
                .build();

        SearchHits<ItemDocument> searchHits = elasticsearchOperations.search(query, ItemDocument.class);

        List<ItemDocument> items = searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .toList();

        return items.isEmpty() ? Optional.empty() : Optional.of(items);
    }
}
