package com.naru.backend.util;

import java.text.ParseException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.naru.backend.security.UserPrincipal;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    public String generateToken(UserPrincipal userDetails) {
        try {
            // JWT Claims 설정
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(userDetails.getEmail())
                    .claim("username", userDetails.getUsername())
                    .claim("role", userDetails.getRole())
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10시간
                    .build();

            // 서명 알고리즘 및 서명 키 설정
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claimsSet);

            signedJWT.sign(new MACSigner(SECRET_KEY.getBytes()));

            // JWT를 직렬화하여 토큰 생성
            return signedJWT.serialize();

        } catch (JOSEException e) {
            throw new RuntimeException("토큰 생성 중 오류 발생", e);
        }
    }

    // JWT에서 사용자 이름을 추출하는 메소드
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public String extractUsername(String token) {
        try {
            return getClaims(token).getStringClaim("username");
        } catch (ParseException e) {
            throw new RuntimeException("토큰 파싱 중 오류 발생", e);
        }
    }

    // JWT의 유효성을 확인하는 메소드
    public boolean isTokenValid(String token, UserPrincipal userDetails) {
        final String email = extractEmail(token);
        return (email.equals(userDetails.getEmail()) && !isTokenExpired(token));
    }

    // JWT의 만료 여부를 확인하는 메소드
    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpirationTime().before(new Date());
    }

    public JWTClaimsSet getClaims(String token) {
        try {
            // JWT 토큰 파싱
            SignedJWT signedJWT = SignedJWT.parse(token);

            // 서명이 유효한지 검증하고, 유효하면 클레임 반환
            return signedJWT.getJWTClaimsSet();

        } catch (ParseException e) {
            throw new RuntimeException("토큰을 파싱하는 중 오류 발생", e);
        }
    }

    public String generateRefreshToken(UserPrincipal userDetails) {
        try {
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(userDetails.getEmail())
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7)) // 7일
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claimsSet);

            signedJWT.sign(new MACSigner(SECRET_KEY.getBytes()));
            return signedJWT.serialize();

        } catch (JOSEException e) {
            throw new RuntimeException("리프레시 토큰 생성 중 오류 발생", e);
        }
    }
}
