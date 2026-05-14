package com.coolai.admin.controller;

import com.coolai.admin.service.SystemService;
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
@Tag(name = "Monitor", description = "System monitoring APIs")
public class MonitorController {

    private final SystemService systemService;

    @GetMapping("/stats")
    @Operation(summary = "Get system statistics")
    public ApiResponse<Map<String, Object>> getStats() {
        return ApiResponse.ok(systemService.getSystemStats());
    }

    @GetMapping("/models")
    @Operation(summary = "List available LLM models")
    public ApiResponse<List<Map<String, Object>>> listModels() {
        return ApiResponse.ok(systemService.getAvailableModels());
    }

    @GetMapping("/config")
    @Operation(summary = "Get system configuration")
    public ApiResponse<Map<String, Object>> getConfig() {
        return ApiResponse.ok(systemService.getSystemConfig());
    }
}
