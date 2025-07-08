package com.kcs3.auction.dto;

import java.util.List;

public record HotAndNewItemListDto(
    List<HotAndNewItemDto> hotAndNewItemDtos
) {
    public static HotAndNewItemListDto of(List<HotAndNewItemDto> hotAndNewItemDtos) {
        return new HotAndNewItemListDto(hotAndNewItemDtos);
    }
}
