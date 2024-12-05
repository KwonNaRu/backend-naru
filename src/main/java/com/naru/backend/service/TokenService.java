package com.naru.backend.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.naru.backend.util.CookieUtil;

import jakarta.servlet.http.HttpServletResponse;

import java.util.concurrent.TimeUnit;

@Service
public class TokenService {

    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60; // 7일

    private final RedisTemplate<String, String> redisTemplate;
    private final CookieUtil cookieUtil;

    public TokenService(RedisTemplate<String, String> redisTemplate, CookieUtil cookieUtil) {
        this.redisTemplate = redisTemplate;
        this.cookieUtil = cookieUtil;
    }

    public void saveRefreshToken(String email, String refreshToken) {
        String key = "refresh_token:" + email;
        redisTemplate.opsForValue().set(key, refreshToken, REFRESH_TOKEN_EXPIRATION, TimeUnit.SECONDS);
    }

    public void saveAccessToken(String email, String accessToken) {
        String key = "access_token:" + email;
        redisTemplate.opsForValue().set(key, accessToken, 10, TimeUnit.HOURS); // 10시간
    }

    public String getRefreshToken(String email) {
        String key = "refresh_token:" + email;
        return redisTemplate.opsForValue().get(key);
    }

    public String getAccessToken(String email) {
        String key = "access_token:" + email;
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteTokens(String email) {
        redisTemplate.delete("refresh_token:" + email);
        redisTemplate.delete("access_token:" + email);
    }

    public void deleteTokenCookies(HttpServletResponse response) {
        cookieUtil.deleteTokenCookies(response);
    }

    public boolean validateRefreshToken(String email, String refreshToken) {
        String storedToken = getRefreshToken(email);
        return refreshToken.equals(storedToken);
    }
}