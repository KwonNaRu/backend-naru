package com.naru.backend.util;

import java.time.Instant;
import java.util.UUID;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TokenUtil {
    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public static String generateTokenWithTimestamp() {
        return Instant.now().getEpochSecond() + "-" + UUID.randomUUID().toString();
    }

    public static boolean isValidToken(String token) {
        try {
            UUID.fromString(token);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
