package com.kcs3.auction.repository;

import com.kcs3.auction.entity.QnaComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QnaCommentRepository extends JpaRepository<QnaComment, Long> {
}
