package com.kcs3.auction.repository;


import com.kcs3.auction.entity.Region;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    // 이름으로 지역 찾기
    Region findByRegion(String region);
    // 전체 지역 ID 리스트 조회
    @Query("SELECT r.regionId FROM Region r")
    List<Long> findAllRegionIds();
}