package com.coolai.voice.controller;

import com.coolai.common.model.ApiResponse;
import com.coolai.voice.engine.CloneEngine;
import com.coolai.voice.service.VoiceCloneService;
import com.coolai.model.entity.VoiceModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/voice")
@RequiredArgsConstructor
@Tag(name = "Voice Clone", description = "Voice cloning APIs")
public class VoiceCloneController {

    private final VoiceCloneService voiceCloneService;

    @PostMapping("/models")
    @Operation(summary = "Create voice model")
    public ApiResponse<VoiceModel> createModel(@RequestBody Map<String, String> request) {
        String userId = getCurrentUserId();
        return ApiResponse.ok(voiceCloneService.createModel(
                userId, request.get("name"), request.get("description")));
    }

    @PostMapping("/synthesize")
    @Operation(summary = "Clone voice synthesis")
    public ApiResponse<CloneEngine.CloneResult> synthesize(@RequestBody Map<String, String> request) {
        String userId = getCurrentUserId();
        return ApiResponse.ok(voiceCloneService.synthesize(
                userId,
                request.get("text"),
                request.get("voiceModelId"),
                request.getOrDefault("engine", "edge_simulate")
        ));
    }

    @GetMapping("/engines")
    @Operation(summary = "Get voice clone engine info")
    public ApiResponse<List<Map<String, Object>>> getEngineInfo() {
        return ApiResponse.ok(voiceCloneService.getEngineInfo());
    }

    private String getCurrentUserId() {
        return (String) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
    }
}
