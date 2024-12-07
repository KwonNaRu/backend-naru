package com.naru.backend.interceptor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && accessor.getDestination() != null) {
            String destination = accessor.getDestination();

            // /topic으로 시작하는 구독은 인증 없이 허용
            if (destination.startsWith("/topic")) {
                return message;
            }

            // /app으로 시작하는 발행은 인증된 사용자만 허용
            if (destination.startsWith("/app")) {
                // 세션에서 username 확인
                String role = (String) accessor.getSessionAttributes().get("role");
                if (!role.equals("OWNER")) {
                    throw new AccessDeniedException("인증되지 않은 사용자입니다.");
                }
            }
        }

        return message;
    }
}