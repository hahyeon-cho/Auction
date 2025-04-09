package com.kcs3.auction.repository;

import com.kcs3.auction.entity.Category;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // 카테고리명으로 카테고리 조회
    Optional<Category> findByCategory(String category);

    // 카테고리명으로 카테고리 ID 조회
    @Query("SELECT c.id FROM Category c WHERE c.categoryName = :name")
    Long findIdByCategory(@Param("name") String name);
}