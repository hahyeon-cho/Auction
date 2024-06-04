package com.kcs3.auction.repository;

import com.kcs3.auction.entity.Alarm;
import com.kcs3.auction.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findTop4ByUserOrderByCreatedAtDesc(User user);

}
