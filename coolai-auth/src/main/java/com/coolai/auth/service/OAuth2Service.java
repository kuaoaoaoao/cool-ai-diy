package com.coolai.auth.service;

import com.coolai.auth.mapper.UserMapper;
import com.coolai.common.exception.BusinessException;
import com.coolai.common.util.JwtUtil;
import com.coolai.model.dto.AuthResponse;
import com.coolai.model.entity.User;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * OAuth2 Service - Handle third-party login (GitHub/Google)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse handleOAuth2Login(String provider, String oauthId, String email, String name) {
        // Check if user exists by OAuth provider info stored in config
        User user = findUserByOAuth(provider, oauthId);

        if (user == null) {
            // Auto-register new OAuth user
            user = new User();
            user.setUsername(name != null ? name : provider + "_" + oauthId);
            user.setEmail(email != null ? email : oauthId + "@" + provider + ".oauth");
            user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setRole("user");
            user.setIsActive(true);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            Map<String, Object> config = new HashMap<>();
            Map<String, String> oauthInfo = new HashMap<>();
            oauthInfo.put("id", oauthId);
            oauthInfo.put("provider", provider);
            config.put("oauth", oauthInfo);
            user.setConfig(config);

            // Ensure unique username
            while (userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, user.getUsername())) != null) {
                user.setUsername(user.getUsername() + "_" + UUID.randomUUID().toString().substring(0, 4));
            }

            userMapper.insert(user);
            log.info("Created new OAuth2 user: provider={}, username={}", provider, user.getUsername());
        }

        return jwtService.generateTokens(user.getId(), user.getUsername(), user.getRole());
    }

    private User findUserByOAuth(String provider, String oauthId) {
        // Search users with matching OAuth config
        // In production, this would use a dedicated oauth_accounts table
        // Simplified: check config field
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                        .eq(User::getIsActive, true))
                .stream()
                .filter(u -> {
                    Map<String, Object> config = u.getConfig();
                    if (config != null && config.containsKey("oauth")) {
                        @SuppressWarnings("unchecked")
                        Map<String, String> oauth = (Map<String, String>) config.get("oauth");
                        return provider.equals(oauth.get("provider")) && oauthId.equals(oauth.get("id"));
                    }
                    return false;
                })
                .findFirst()
                .orElse(null);
    }
}
