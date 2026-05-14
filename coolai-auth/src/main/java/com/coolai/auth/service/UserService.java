package com.coolai.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coolai.auth.mapper.UserMapper;
import com.coolai.common.exception.BusinessException;
import com.coolai.model.dto.AuthResponse;
import com.coolai.model.dto.LoginRequest;
import com.coolai.model.dto.ProfileUpdateRequest;
import com.coolai.model.dto.RegisterRequest;
import com.coolai.model.entity.User;
import lombok.RequiredArgsConstructor;
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

    public User getCurrentUser(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("User not found");
        }
        return user;
    }

    @Transactional
    public User updateProfile(String userId, ProfileUpdateRequest request) {
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
        return user;
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
    public Map<String, String> getApiKeys(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.notFound("User not found");
        }

        Map<String, Object> config = user.getConfig();
        if (config == null || !config.containsKey("apiKeys")) {
            return new HashMap<>();
        }
        return (Map<String, String>) config.get("apiKeys");
    }
}
