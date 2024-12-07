package com.naru.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.naru.backend.interceptor.AuthHandshakeInterceptor;
import com.naru.backend.interceptor.StompAuthChannelInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final AuthHandshakeInterceptor authHandshakeInterceptor;
    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    public WebSocketConfig(AuthHandshakeInterceptor authHandshakeInterceptor,
            StompAuthChannelInterceptor stompAuthChannelInterceptor) {
        this.authHandshakeInterceptor = authHandshakeInterceptor;
        this.stompAuthChannelInterceptor = stompAuthChannelInterceptor;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws", "/subscriptions")
                .addInterceptors(authHandshakeInterceptor)
                .setAllowedOriginPatterns("http://localhost:3000", "https://mixed-naru.vercel.app")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(stompAuthChannelInterceptor);
    }

}