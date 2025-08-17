package com.kcs3.auction.config;

import com.kcs3.auction.entity.Category;
import com.kcs3.auction.entity.TradingMethod;
import com.kcs3.auction.repository.CategoryRepository;
import com.kcs3.auction.repository.TradingMethodRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final CategoryRepository categoryRepository;
    private final TradingMethodRepository tradingMethodRepository;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToTradingMethodConverter());
        registry.addConverter(new StringToCategoryConverter());
    }

    // 문자열 ID를 TradingMethod 엔티티로 변환하는 컨버터
    private class StringToTradingMethodConverter implements Converter<String, TradingMethod> {

        @Override
        public TradingMethod convert(@NonNull String source) {
            Long id = Long.parseLong(source);
            return tradingMethodRepository.findById(id)
                .orElseThrow(
                    () -> new IllegalArgumentException("Invalid TradingMethod ID: " + source));
        }
    }

    // 문자열 이름을 Category 엔티티로 변환하는 컨버터
    private class StringToCategoryConverter implements Converter<String, Category> {

        @Override
        public Category convert(@NonNull String source) {
            return categoryRepository.findByCategoryName(source)
                .orElseThrow(
                    () -> new IllegalArgumentException("Invalid category name: " + source));
        }
    }
}
