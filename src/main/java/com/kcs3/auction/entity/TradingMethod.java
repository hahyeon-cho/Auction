package com.kcs3.auction.entity;

import com.kcs3.auction.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "TradingMethod")
@DynamicUpdate
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TradingMethod extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tradingMethodId", nullable = false)
    private Long tradingMethodId;

    @Column(nullable = false)
    private int tradingMethod;

    @Builder
    public TradingMethod(int tradingMethod) {
        this.tradingMethod = tradingMethod;
    }
}