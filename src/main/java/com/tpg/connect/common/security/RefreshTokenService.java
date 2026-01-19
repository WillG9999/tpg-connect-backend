package com.tpg.connect.common.security;

import com.tpg.connect.common.jsonwebtoken.components.JsonWebTokenProvider;
import com.tpg.connect.common.jsonwebtoken.components.JsonWebTokenValidator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    private static final long REFRESH_TOKEN_TTL_DAYS = 7;

    private final RedisTemplate<String, Object> redisTemplate;
    private final JsonWebTokenProvider tokenProvider;
    private final JsonWebTokenValidator tokenValidator;

    public void storeRefreshToken(long connectId, String refreshToken) {
        try {
            String key = REFRESH_TOKEN_PREFIX + connectId;
            redisTemplate.opsForValue().set(key, refreshToken, REFRESH_TOKEN_TTL_DAYS, TimeUnit.DAYS);
            log.info("Refresh token stored for connectId: {}", connectId);
        } catch (Exception e) {
            log.warn("Failed to store refresh token: {}", e.getMessage());
        }
    }

    public Optional<String> getStoredRefreshToken(long connectId) {
        try {
            String key = REFRESH_TOKEN_PREFIX + connectId;
            Object token = redisTemplate.opsForValue().get(key);
            return Optional.ofNullable(token).map(Object::toString);
        } catch (Exception e) {
            log.warn("Failed to get refresh token: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public void revokeRefreshToken(long connectId) {
        try {
            String key = REFRESH_TOKEN_PREFIX + connectId;
            redisTemplate.delete(key);
            log.info("Refresh token revoked for connectId: {}", connectId);
        } catch (Exception e) {
            log.warn("Failed to revoke refresh token: {}", e.getMessage());
        }
    }

    public Optional<TokenPair> refreshAccessToken(String refreshToken, String email, String role) {
        if (!tokenValidator.isValidToken(refreshToken) || !tokenValidator.isRefreshToken(refreshToken)) {
            log.warn("Invalid refresh token");
            return Optional.empty();
        }

        Claims claims = tokenValidator.getClaims(refreshToken);
        long connectId = Long.parseLong(claims.getSubject());

        Optional<String> storedToken = getStoredRefreshToken(connectId);
        if (storedToken.isEmpty() || !storedToken.get().equals(refreshToken)) {
            log.warn("Refresh token not found or mismatch for connectId: {}", connectId);
            return Optional.empty();
        }

        String newAccessToken = tokenProvider.generateToken(connectId, email, role);
        String newRefreshToken = tokenProvider.generateRefreshToken(connectId);

        storeRefreshToken(connectId, newRefreshToken);

        return Optional.of(new TokenPair(newAccessToken, newRefreshToken));
    }

    public record TokenPair(String accessToken, String refreshToken) {}
}

