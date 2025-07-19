package com.kcs3.auction.document;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemSearchDocument {

    @Id
    private Long itemId;
    private String itemTitle;
    private LocalDateTime createdAt;

    @Builder
    public ItemSearchDocument(Long itemId, String itemTitle, LocalDateTime createdAt) {
        this.itemId = itemId;
        this.itemTitle = itemTitle;
        this.createdAt = createdAt;
    }
}