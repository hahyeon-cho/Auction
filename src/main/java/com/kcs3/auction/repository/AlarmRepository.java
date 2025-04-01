package com.kcs3.auction.repository;

import com.kcs3.auction.entity.Alarm;
import com.kcs3.auction.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    // 특정 유저의 알림 중 최신순으로 최대 4개 조회
    List<Alarm> findTop4ByUserOrderByCreatedAtDesc(User user);
}
