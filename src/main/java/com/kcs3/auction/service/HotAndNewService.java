package com.kcs3.auction.service;

import com.kcs3.auction.dto.RedisItemDto;
import com.kcs3.auction.dto.RedisItemListDto;
import com.kcs3.auction.entity.AuctionProgressItem;
import com.kcs3.auction.exception.CommonException;
import com.kcs3.auction.exception.ErrorCode;
import com.kcs3.auction.repository.AuctionInfoRepository;
import com.kcs3.auction.repository.AuctionProgressItemRepository;
import com.kcs3.auction.repository.ItemRepository;
import com.kcs3.auction.repository.RegionRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotAndNewService {

    private final ItemRepository itemRepository;
    private final AuctionInfoRepository auctionInfoRepository;
    private final AuctionProgressItemRepository progressItemRepository;
    private final RegionRepository regionRepository;

    private final RedisTemplate<String, RedisItemDto> redisTemplate;

    private static final Pageable CACHE_ITEM_PAGEABLE = PageRequest.of(0, 10);

    // === Hot & New 물품 리스트 조회 ===
    // Redis 캐시 데이터 조회
    private List<RedisItemDto> getItemsFromRedis(String prefix, Long regionId) {
        String key = prefix + regionId;
        try {
            List<RedisItemDto> result = redisTemplate.opsForList().range(key, 0, -1);
            if (result == null) {
                return Collections.emptyList();
            }

            return result.stream()
                .filter(RedisItemDto.class::isInstance)
                .map(RedisItemDto.class::cast)
                .collect(Collectors.toList());

        } catch (RedisConnectionFailureException | RedisSystemException e) {
            log.warn("Redis 연결 실패: {}", e.getMessage(), e);
            throw new CommonException(ErrorCode.REDIS_CONNECTION_FAILED);
        }
    }

    // Hot 아이템 목록 Redis 조회
    public RedisItemListDto getHotItemsByRegion(String regionName) {
        Long regionId = Optional.ofNullable(regionRepository.findIdByRegionName(regionName))
            .orElse(1L);

        List<RedisItemDto> items = getItemsFromRedis("hot_items:", regionId);

        if (items.isEmpty() && regionId != 1L) {
            items = getItemsFromRedis("hot_items:", 1L);  // fallback
        }

        if (items.isEmpty()) {
            throw new CommonException(ErrorCode.ITEM_CACHE_NOT_FOUND);
        }

        return RedisItemListDto.builder()
            .redisItemDtos(items)
            .build();
    }

    // New 아이템 목록 Redis 조회
    public RedisItemListDto getNewItemsByRegion(String regionName) {
        Long regionId = Optional.ofNullable(regionRepository.findIdByRegionName(regionName))
            .orElse(1L);

        List<RedisItemDto> items = getItemsFromRedis("new_item:", regionId);

        if (items.isEmpty() && regionId != 1L) {
            items = getItemsFromRedis("new_item:", 1L);  // fallback
        }

        if (items.isEmpty()) {
            throw new CommonException(ErrorCode.ITEM_CACHE_NOT_FOUND);
        }

        return RedisItemListDto.builder()
            .redisItemDtos(items)
            .build();
    }

    // === Hot & New 물품 리스트 캐싱 ===
    // 지역별 인기 물품 리스트를 Redis 캐시에 저장
    public void cacheHotItemsByRegion(Long regionId) {
        // 최근 인기 아이템 itemId 리스트 조회
        List<Long> hotItemIds = auctionInfoRepository.findPopularProgressItemIdsByRegion(regionId, CACHE_ITEM_PAGEABLE);

        List<AuctionProgressItem> hotItems = progressItemRepository.findAllWithItemAndCategory(hotItemIds);

        List<RedisItemDto> redisItemDtos = hotItems.stream()
            .map(RedisItemDto::from)
            .collect(Collectors.toList());

        // Redis 저장 (기존 리스트 삭제 후 갱신)
        String redisKey = "hot_items:" + regionId;
        redisTemplate.delete(redisKey);
        redisTemplate.opsForList().leftPushAll(redisKey, redisItemDtos);
    }

    // 지역별 신규 물품 리스트를 Redis 캐시에 저장
    public void cacheNewItemsByRegion(Long regionId) {
        // 신규 아이템 itemId 리스트 조회
        List<Long> newItemIds = itemRepository.findLatestInProgressItemIdsByRegion(regionId,CACHE_ITEM_PAGEABLE);

        List<AuctionProgressItem> newItems = progressItemRepository.findAllWithItemAndCategory(newItemIds);

        List<RedisItemDto> redisItemDtos = newItems.stream()
            .map(RedisItemDto::from)
            .collect(Collectors.toList());

        // Redis 저장 (기존 리스트 삭제 후 갱신)
        String redisKey = "new_items:" + regionId;
        redisTemplate.delete(redisKey);
        redisTemplate.opsForList().leftPushAll(redisKey, redisItemDtos);
    }

    // [Todo]: TTL 적용 검토
    @Scheduled(cron = "* */5 * * * *")
    public void refreshAllItemCaches() {
        List<Long> regionIds = regionRepository.findAllRegionIds();

        for (Long regionId : regionIds) {
            cacheHotItemsByRegion(regionId);
            cacheNewItemsByRegion(regionId);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.warn("물품 리스트 캐싱 처리 중 sleep 상태에서 인터럽트가 발생했습니다.: {}", regionId, e);
                Thread.currentThread().interrupt();
            }
        }
    }
}
