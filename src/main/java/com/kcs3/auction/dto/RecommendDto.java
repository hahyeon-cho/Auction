package com.kcs3.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendDto {
    private Long itemId;
    private String itemTitle;
    private String thumbnail;
    private Integer maxPrice;
}
