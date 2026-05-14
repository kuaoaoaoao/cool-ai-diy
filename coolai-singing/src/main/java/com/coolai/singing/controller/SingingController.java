package com.coolai.singing.controller;

import com.coolai.common.model.ApiResponse;
import com.coolai.model.dto.SingingRequest;
import com.coolai.model.entity.SingingTask;
import com.coolai.singing.service.SingingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/singing")
@RequiredArgsConstructor
@Tag(name = "Singing", description = "AI Singing APIs")
public class SingingController {

    private final SingingService singingService;

    @PostMapping("/synthesize")
    @Operation(summary = "Lyrics-driven singing synthesis")
    public ApiResponse<SingingTask> synthesize(@Valid @RequestBody SingingRequest request) {
        String userId = getCurrentUserId();
        return ApiResponse.ok(singingService.synthesize(userId, request));
    }

    @GetMapping("/styles")
    @Operation(summary = "List available singing styles")
    public ApiResponse<List<Map<String, String>>> listStyles() {
        return ApiResponse.ok(singingService.getEngine("edge_tts").listStyles());
    }

    @GetMapping("/engines")
    @Operation(summary = "Get singing engine info")
    public ApiResponse<List<Map<String, Object>>> getEngineInfo() {
        return ApiResponse.ok(singingService.getEngineInfo());
    }

    private String getCurrentUserId() {
        return (String) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
    }
}
