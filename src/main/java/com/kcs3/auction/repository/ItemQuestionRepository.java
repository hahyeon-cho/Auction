package com.kcs3.auction.repository;

import com.kcs3.auction.entity.ItemQuestion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemQuestionRepository extends JpaRepository<ItemQuestion, Long> {
    List<ItemQuestion> findByItemDetailId_ItemDetailId(Long itemDetailId);
}
