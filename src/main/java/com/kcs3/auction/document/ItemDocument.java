package com.kcs3.auction.document;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;

@Document(indexName = "items")
@Builder
public record ItemDocument(@Id Long itemId, String itemTitle, LocalDateTime createAt) {
}
