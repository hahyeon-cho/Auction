package com.kcs3.auction.repository;

import com.kcs3.auction.entity.TradingMethod;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TradingMethodRepository extends JpaRepository<TradingMethod, Long> {

    // 거래방법 코드로 거래방법 조회
    Optional<TradingMethod> findByTradingMethod(Integer method);

    // 거래방법 코드로 거래방법 ID 조회
    @Query("SELECT t.id FROM TradingMethod t WHERE t.tmCode = :code")
    Long findIdByTradingMethod(@Param("code") Integer code);
}
