package com.naru.backend.interceptor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.naru.backend.security.UserPrincipal;
import com.naru.backend.util.JwtUtil;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtChannelInterceptor(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand()) ||
                StompCommand.SEND.equals(accessor.getCommand())) {

            String jwtToken = accessor.getFirstNativeHeader("Authorization");

            if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
                jwtToken = jwtToken.substring(7);
                try {
                    String username = jwtUtil.extractUsername(jwtToken);
                    UserPrincipal userDetails = (UserPrincipal) userDetailsService.loadUserByUsername(username);

                    if (jwtUtil.isTokenValid(jwtToken, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        accessor.setUser(authentication);
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid JWT token");
                }
            }
        }

        return message;
    }
}
