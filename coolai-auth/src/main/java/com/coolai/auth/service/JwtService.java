package com.coolai.auth.service;

import com.coolai.common.util.JwtUtil;
import com.coolai.model.dto.AuthResponse;
import com.coolai.model.dto.RefreshTokenRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${coolai.jwt.access-token-expiration:3600000}")
    private long accessTokenExpiration;

    @Value("${coolai.jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;

    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";

    public AuthResponse generateTokens(String userId, String username, String role) {
        String accessToken = jwtUtil.generateAccessToken(userId, username, role);
        String refreshToken = jwtUtil.generateRefreshToken(userId, username, role);

        // Store refresh token in Redis
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                refreshToken,
                refreshTokenExpiration,
                TimeUnit.MILLISECONDS
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(accessTokenExpiration / 1000)
                .user(AuthResponse.UserInfo.builder()
                        .id(userId)
                        .username(username)
                        .role(role)
                        .build())
                .build();
    }

    public AuthResponse refreshTokens(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String userId = jwtUtil.getUserIdFromToken(refreshToken);

        // Verify refresh token in Redis
        Object storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new RuntimeException("Refresh token not found or expired");
        }

        String username = jwtUtil.getUsernameFromToken(refreshToken);
        String role = jwtUtil.getRoleFromToken(refreshToken);

        return generateTokens(userId, username, role);
    }

    public void revokeRefreshToken(String userId) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
    }
}
