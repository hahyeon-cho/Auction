package com.kcs3.auction.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws UnsupportedEncodingException {

        CustomOAuth2User customUserDetail = (CustomOAuth2User) authentication.getPrincipal();

        Long userId = customUserDetail.getUserId();
        String email = customUserDetail.getEmail();
        String nickname = customUserDetail.getName();
        String encodedNickname = URLEncoder.encode(nickname, "UTF-8");

        String accessToken = jwtUtil.createJwt("access", userId, email, 600000000L);
        String refreshToken = jwtUtil.createJwt("refresh", userId, email, 86400000L);

        String redirectUrl =
            frontendUrl + "/login-success?access=Bearer " + accessToken
                + "&id=" + userId + "&username=" + encodedNickname;

        response.setStatus(HttpStatus.OK.value());

        try {
            response.sendRedirect(redirectUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
