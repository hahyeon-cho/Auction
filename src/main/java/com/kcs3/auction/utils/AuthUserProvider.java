package com.kcs3.auction.utils;

import com.kcs3.auction.entity.User;
import com.kcs3.auction.exception.CommonException;
import com.kcs3.auction.exception.ErrorCode;
import com.kcs3.auction.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 인증된 사용자 정보를 제공하는 컴포넌트입니다.
 * <p>
 * JWTFilter에 의해 SecurityContext에 저장된 인증 객체(Authentication)로부터 사용자 ID를 추출하고, 실제 DB에서 최신 사용자 정보를 조회합니다.
 * <p>
 * 사용 예: User currentUser = authUserProvider.getCurrentUser();
 * <pre>
 * 주의:
 * - 인증 객체가 없거나 인증되지 않은 경우, INVALID_TOKEN_ERROR 예외 발생
 * - DB에 사용자 정보가 존재하지 않을 경우, NOT_FOUND_USER 예외 발생
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class AuthUserProvider {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CommonException(ErrorCode.TOKEN_INVALID);
        }

        CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();

        return userRepository.findById(principal.getUserId())
            .orElseThrow(() -> new CommonException(ErrorCode.USER_NOT_FOUND));
    }
}