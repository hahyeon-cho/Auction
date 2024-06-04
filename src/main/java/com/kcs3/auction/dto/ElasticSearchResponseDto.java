package com.kcs3.auction.dto;

import java.util.List;

public record ElasticSearchResponseDto(Hits hits) {

    public record Hits(List<Hit> hits) {
        public record Hit(Source source) {
            public record Source(Long itemId) {
            }
        }
    }
}

