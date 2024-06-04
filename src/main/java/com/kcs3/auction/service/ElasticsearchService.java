package com.kcs3.auction.service;


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
                        .match(m -> m
                                .field("itemTitle")
                                .query(keyword)
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



