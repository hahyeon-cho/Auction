package com.kcs3.auction.repository;

import com.kcs3.auction.entity.Region;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

    // 지명으로 지역 조회
    Optional<Region> findByRegionName(String region);

    // 지명으로 지역 ID 조회
    @Query("SELECT r.id FROM Region r WHERE r.regionName = :name")
    Long findIdByRegionName(@Param("name") String name);

    // 전체 지역 ID 리스트 조회
    @Query("SELECT r.regionId FROM Region r")
    List<Long> findAllRegionIds();
}
