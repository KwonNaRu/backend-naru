package com.naru.backend.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.naru.backend.security.UserPrincipal;
import com.naru.backend.service.TokenService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CookieUtil {
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    public CookieUtil(JwtUtil jwtUtil, TokenService tokenService) {
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
    }

    public Cookie setAccessTokenCookie(String accessToken) {
        Cookie accessTokenCookie = new Cookie("NID_AUTH", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true); // HTTPS에서만 전송되도록 설정 (필요에 따라)
        accessTokenCookie.setPath("/");
        return accessTokenCookie;
    }

    public Cookie setRefreshTokenCookie(String refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true); // HTTPS에서만 전송
        refreshTokenCookie.setPath("/");
        return refreshTokenCookie;
    }

    public void addTokenCookies(HttpServletResponse response, Map<String, String> tokens) {
        Cookie accessTokenCookie = setAccessTokenCookie(tokens.get("NID_AUTH"));
        Cookie refreshTokenCookie = setRefreshTokenCookie(tokens.get("refreshToken"));
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }

    public Map<String, String> generateTokens(UserPrincipal userPrincipal, String email) {
        Map<String, String> tokens = new HashMap<>();
        String accessToken = refreshAccessToken(userPrincipal, email);
        String refreshToken = refreshRefreshToken(userPrincipal, email);
        tokens.put("NID_AUTH", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    public String refreshAccessToken(UserPrincipal userPrincipal, String email) {
        String accessToken = jwtUtil.generateToken(userPrincipal);

        // Redis에 토큰 저장
        tokenService.saveAccessToken(email, accessToken);

        return accessToken;
    }

    public String refreshRefreshToken(UserPrincipal userPrincipal, String email) {
        String refreshToken = jwtUtil.generateRefreshToken(userPrincipal);

        // Redis에 토큰 저장
        tokenService.saveRefreshToken(email, refreshToken);

        return refreshToken;
    }
}