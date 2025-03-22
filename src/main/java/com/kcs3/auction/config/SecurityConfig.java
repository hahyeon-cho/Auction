package com.kcs3.auction.config;

import com.kcs3.auction.utils.CustomSuccessHandler;
import com.kcs3.auction.utils.JWTFilter;
import com.kcs3.auction.utils.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${frontend.url}")
    private String frontendUrl;

    private final JWTUtil jwtUtil;
    private final CustomSuccessHandler customSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(@NonNull HttpSecurity http) throws Exception {

        // CORS 설정
        http.cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {
            @Override
            public CorsConfiguration getCorsConfiguration(@NonNull HttpServletRequest request) {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Collections.singletonList(frontendUrl));
                configuration.setAllowedMethods(Collections.singletonList("*"));
                configuration.setAllowedHeaders(Collections.singletonList("*"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                // 클라이언트에 노출할 헤더
                configuration.setExposedHeaders(Collections.singletonList("Authorization"));
                return configuration;
            }
        }));

        // CSRF, 로그인, HTTP Basic 비활성화
        http.csrf(csrf -> csrf.disable());
        http.formLogin(form -> form.disable());
        http.httpBasic(basic -> basic.disable());

        // 세션 사용하지 않음 (JWT 기반)
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // JWT 필터 등록
        http.addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        // 경로별 인가 설정
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/api/v1/no-auth/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/ws/**",
                        "/actuator/**"
                ).permitAll()
                .anyRequest().authenticated()
        );

        return http.build();
    }
}