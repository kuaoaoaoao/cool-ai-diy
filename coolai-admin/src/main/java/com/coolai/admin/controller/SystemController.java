package com.coolai.admin.controller;

import com.coolai.common.llm.LlmGatewayService;
import com.coolai.common.model.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/system")
@RequiredArgsConstructor
@Tag(name = "System", description = "System management APIs")
public class SystemController {

    private final LlmGatewayService llmGatewayService;

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ApiResponse<Map<String, Object>> health() {
        return ApiResponse.ok(Map.of(
                "status", "UP",
                "timestamp", System.currentTimeMillis()
        ));
    }

    @GetMapping("/models")
    @Operation(summary = "List available LLM models")
    public ApiResponse<List<Map<String, Object>>> listModels() {
        return ApiResponse.ok(llmGatewayService.listModels());
    }

    @GetMapping("/stats")
    @Operation(summary = "Get system statistics")
    public ApiResponse<Map<String, Object>> getStats() {
        return ApiResponse.ok(Map.of(
                "totalUsers", 0,
                "totalConversations", 0,
                "totalMessages", 0,
                "uptime", System.currentTimeMillis()
        ));
    }
}
