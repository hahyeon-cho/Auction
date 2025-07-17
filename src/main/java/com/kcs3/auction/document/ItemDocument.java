package com.kcs3.auction.document;

import java.time.LocalDateTime;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "items")
@Builder
public class ItemDocument{
    @Id
    private Long itemId;

    private String itemTitle;
    private Long categoryId;
    private Long regionId;
    private Long tradingMethodId;
    private Boolean isAuctionComplete;
    private LocalDateTime createdAt;

    @Field(type = FieldType.Dense_Vector, dims = 1536)
    private float[] embedding;

    @Field(type = FieldType.Text, analyzer = "korean")
    private String searchText;
}