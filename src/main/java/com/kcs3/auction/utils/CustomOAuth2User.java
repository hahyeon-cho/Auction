package com.kcs3.auction.utils;

import com.kcs3.auction.dto.UserDto;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final UserDto userDto;

    @Override
    public Map<String, Object> getAttributes() { return null; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        authorities.add(() -> "유저");

        return authorities;
    }

    @Override
    public String getName() { return userDto.getNickname(); }

    public String getEmail() { return userDto.getEmail(); }

    public Long getUserId() { return userDto.getUserId(); }
}
