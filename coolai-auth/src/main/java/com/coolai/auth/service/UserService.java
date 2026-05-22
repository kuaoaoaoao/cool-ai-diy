package com.coolai.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coolai.auth.mapper.UserMapper;
import com.coolai.common.exception.BusinessException;
import com.coolai.model.dto.ApiKeyStatus;
import com.coolai.model.dto.AuthResponse;
import com.coolai.model.dto.LoginRequest;
import com.coolai.model.dto.ProfileUpdateRequest;
import com.coolai.model.dto.RegisterRequest;
import com.coolai.model.dto.UserProfileResponse;
import com.coolai.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${coolai.llm.openai.api-key:fake-mimo-api-key}")
    private String defaultMimoApiKey;

    @Value("${coolai.llm.anthropic.api-key:fake-anthropic-api-key}")
    private String defaultAnthropicApiKey;

    @Value("${coolai.mcp.tavily.api-key:fake-tavily-api-key}")
    private String defaultTavilyApiKey;

    @Value("${coolai.mcp.openweathermap.api-key:fake-openweathermap-api-key}")
    private String defaultOpenWeatherMapApiKey;

    @Value("${coolai.video.wanx.api-key:fake-wanx-api-key}")
    private String defaultWanxApiKey;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if username already exists
        User existingUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername())
        );
        if (existingUser != null) {
            throw BusinessException.conflict("Username already exists");
        }

        // Check if email already exists
        User existingEmail = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail())
        );
        if (existingEmail != null) {
            throw BusinessException.conflict("Email already exists");
        }

        // Create user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("user");
        user.setConfig(new HashMap<>());
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);

        return jwtService.generateTokens(user.getId(), user.getUsername(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        // Find user by username or email
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, request.getUsername())
                        .or()
                        .eq(User::getEmail, request.getUsername())
        );

        if (user == null) {
            throw BusinessException.unauthorized("Invalid credentials");
        }

        if (!user.getIsActive()) {
            throw BusinessException.forbidden("Account is deactivated");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw BusinessException.unauthorized("Invalid credentials");
        }

        return jwtService.generateTokens(user.getId(), user.getUsername(), user.getRole());
    }

    public UserProfileResponse getCurrentUser(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("User not found");
        }
        return toProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(String userId, ProfileUpdateRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("User not found");
        }

        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getConfig() != null) {
            user.setConfig(request.getConfig());
        }
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.updateById(user);
        return toProfileResponse(user);
    }

    @Transactional
    public void updateApiKeys(String userId, Map<String, String> apiKeys) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("User not found");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> config = user.getConfig() != null
                ? new HashMap<>(user.getConfig())
                : new HashMap<>();
        config.put("apiKeys", apiKeys);

        user.setConfig(config);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @SuppressWarnings("unchecked")
    public Map<String, ApiKeyStatus> getApiKeys(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("User not found");
        }

        Map<String, Object> config = user.getConfig();
        Map<String, String> apiKeys = config != null && config.containsKey("apiKeys")
                ? (Map<String, String>) config.get("apiKeys")
                : new HashMap<>();

        Map<String, ApiKeyStatus> statuses = new HashMap<>();
        statuses.put("mimo", buildApiKeyStatus("mimo",
                apiKeys.getOrDefault("mimo", apiKeys.getOrDefault("openai", defaultMimoApiKey))));
        statuses.put("anthropic", buildApiKeyStatus("anthropic", apiKeys.getOrDefault("anthropic", defaultAnthropicApiKey)));
        statuses.put("tavily", buildApiKeyStatus("tavily", apiKeys.getOrDefault("tavily", defaultTavilyApiKey)));
        statuses.put("openweathermap", buildApiKeyStatus("openweathermap",
                apiKeys.getOrDefault("openweathermap", defaultOpenWeatherMapApiKey)));
        statuses.put("wanx", buildApiKeyStatus("wanx", apiKeys.getOrDefault("wanx", defaultWanxApiKey)));
        return statuses;
    }

    private UserProfileResponse toProfileResponse(User user) {
        Map<String, Object> safeConfig = user.getConfig() != null
                ? new HashMap<>(user.getConfig())
                : new HashMap<>();
        safeConfig.remove("apiKeys");

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .config(safeConfig)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private ApiKeyStatus buildApiKeyStatus(String provider, String value) {
        boolean configured = value != null && !value.isBlank();
        boolean placeholder = configured && value.startsWith("fake-");
        return ApiKeyStatus.builder()
                .provider(provider)
                .configured(configured)
                .placeholder(placeholder)
                .maskedValue(maskApiKey(value))
                .build();
    }

    private String maskApiKey(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        if (value.startsWith("fake-")) {
            return value;
        }
        if (value.length() <= 8) {
            return "****";
        }
        return value.substring(0, 4) + "****" + value.substring(value.length() - 4);
    }
}
