package com.coolai.auth.controller;

import com.coolai.auth.service.JwtService;
import com.coolai.auth.service.UserService;
import com.coolai.common.model.ApiResponse;
import com.coolai.model.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and authorization APIs")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    @Operation(summary = "User registration")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(userService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "User login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(userService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.ok(jwtService.refreshTokens(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user info")
    public ApiResponse<UserProfileResponse> getCurrentUser() {
        String userId = getCurrentUserId();
        return ApiResponse.ok(userService.getCurrentUser(userId));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile")
    public ApiResponse<UserProfileResponse> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        String userId = getCurrentUserId();
        return ApiResponse.ok(userService.updateProfile(userId, request));
    }

    @PutMapping("/api-keys")
    @Operation(summary = "Save API keys")
    public ApiResponse<Void> updateApiKeys(@RequestBody ApiKeyRequest request) {
        String userId = getCurrentUserId();
        userService.updateApiKeys(userId, request.getApiKeys());
        return ApiResponse.ok();
    }

    @GetMapping("/api-keys")
    @Operation(summary = "Get API keys status")
    public ApiResponse<Map<String, ApiKeyStatus>> getApiKeys() {
        String userId = getCurrentUserId();
        return ApiResponse.ok(userService.getApiKeys(userId));
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (String) authentication.getPrincipal();
    }
}
