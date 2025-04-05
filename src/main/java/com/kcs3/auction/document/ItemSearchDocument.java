package com.kcs3.auction.document;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "items")
@Builder
public record ItemSearchDocument(
    @Id Long itemId,
    String itemTitle,
    Long createdAt
) {

}