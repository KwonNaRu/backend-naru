package com.naru.backend.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.stereotype.Component;

import com.naru.backend.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;

    public AuthHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();

            // 쿠키에서 JWT 추출
            Cookie[] cookies = httpServletRequest.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("NID_AUTH".equals(cookie.getName())) {
                        String token = cookie.getValue();
                        try {
                            String role = jwtUtil.extractRole(token); // JWT 검증
                            attributes.put("role", role); // WebSocket 세션에 사용자 정보 저장
                            return true;
                        } catch (Exception e) {
                            System.out.println("JWT 검증 실패: " + e.getMessage());
                            return false; // 검증 실패
                        }
                    }
                }
            }
        }
        System.out.println("JWT 쿠키가 없습니다.");
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception exception) {
        // 핸드셰이크 후 처리 로직
    }
}
