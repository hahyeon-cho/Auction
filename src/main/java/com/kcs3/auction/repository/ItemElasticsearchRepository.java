package com.kcs3.auction.repository;

import com.kcs3.auction.document.ItemDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemElasticsearchRepository extends ElasticsearchRepository<ItemDocument, Long> {
}
