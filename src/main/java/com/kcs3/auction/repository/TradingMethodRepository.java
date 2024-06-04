package com.kcs3.auction.repository;


import com.kcs3.auction.entity.TradingMethod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradingMethodRepository extends JpaRepository<TradingMethod, Long> {
}
