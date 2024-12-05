package com.naru.backend.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.naru.backend.dto.LoginDTO;
import com.naru.backend.dto.UserRequestDTO;
import com.naru.backend.dto.UserResponseDTO;
import com.naru.backend.exception.EmailNotVerifiedException;
import com.naru.backend.model.User;
import com.naru.backend.security.UserPrincipal;
import com.naru.backend.service.TokenService;
import com.naru.backend.service.UserService;
import com.naru.backend.util.CookieUtil;
import com.naru.backend.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;
    private final TokenService tokenService;
    private final CookieUtil cookieUtil;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public UserController(UserService userService, TokenService tokenService, CookieUtil cookieUtil, JwtUtil jwtUtil,
            UserDetailsService userDetailsService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.cookieUtil = cookieUtil;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@RequestBody UserRequestDTO userRequestDTO) {
        return ResponseEntity.ok(userService.registerUser(userRequestDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginDTO loginDTO, HttpServletResponse response) {
        try {

            // 사용자 인증 및 JWT 생성
            User user = userService.authenticateUser(loginDTO);
            Map<String, String> tokens = userService.generateTokens(user);
            cookieUtil.addTokenCookies(response, tokens);

            UserResponseDTO userResponse = new UserResponseDTO(user);

            // 사용자 정보와 refreshToken을 응답으로 반환
            return ResponseEntity.ok(userResponse);
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        } catch (EmailNotVerifiedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("로그인 처리 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token, @RequestParam String email) {
        boolean verified = userService.verifyEmail(email, token);
        if (verified) {
            return ResponseEntity.ok("이메일이 성공적으로 인증되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("유효하지 않은 토큰입니다.");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        try {

            String refreshToken = Arrays.stream(request.getCookies())
                    .filter(cookie -> "refreshToken".equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElseThrow(() -> new RuntimeException("Refresh token not found"));

            String email = jwtUtil.extractUsername(refreshToken);

            // refreshToken 검증 및 새로운 accessToken 발급
            Map<String, String> tokens = userService.refreshAccessToken(refreshToken);
            cookieUtil.addTokenCookies(response, tokens);

            UserResponseDTO userResponse = userService.convertToUserResponseDTO(email);

            return ResponseEntity.ok(userResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> checkTokenStatus(HttpServletRequest request) {
        try {
            String accessToken = Arrays.stream(request.getCookies())
                    .filter(cookie -> "NID_AUTH".equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElseThrow(() -> new RuntimeException("Access token not found"));

            // Refresh Token 추출
            String refreshToken = Arrays.stream(request.getCookies())
                    .filter(cookie -> "refreshToken".equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElseThrow(() -> new RuntimeException("Refresh token not found"));

            String email = jwtUtil.extractUsername(accessToken);

            // Redis에 저장된 토큰과 비교
            String storedToken = tokenService.getAccessToken(email);
            String storedRefreshToken = tokenService.getRefreshToken(email);
            UserPrincipal userDetails = (UserPrincipal) userDetailsService.loadUserByUsername(email);
            if (storedToken.equals(accessToken) && storedRefreshToken.equals(refreshToken)
                    && jwtUtil.isTokenValid(accessToken, userDetails)
                    && jwtUtil.isTokenValid(storedRefreshToken, userDetails)) {
                UserResponseDTO userResponse = userService.convertToUserResponseDTO(email);
                return ResponseEntity.ok(userResponse);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access token is expired or invalid");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid access token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response, @RequestBody String email) {
        try {
            SecurityContextHolder.clearContext();
            tokenService.deleteTokens(email);
            tokenService.deleteTokenCookies(response);
            return ResponseEntity.ok("로그아웃 되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("로그아웃 처리 중 오류가 발생했습니다.");
        }
    }
}
