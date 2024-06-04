package com.kcs3.auction.dto;

import java.util.List;
import lombok.Builder;


@Builder
public record HotItemListDto(
        List<HotItemsDto> hotItemListDtos
) {
}
