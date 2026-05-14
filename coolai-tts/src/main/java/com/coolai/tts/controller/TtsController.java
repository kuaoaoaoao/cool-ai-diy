package com.coolai.tts.controller;

import com.coolai.common.model.ApiResponse;
import com.coolai.model.dto.TtsRequest;
import com.coolai.model.entity.TtsTask;
import com.coolai.tts.service.TtsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tts")
@RequiredArgsConstructor
@Tag(name = "TTS", description = "Text-to-Speech APIs")
public class TtsController {

    private final TtsService ttsService;

    @GetMapping("/voices")
    @Operation(summary = "List available voices")
    public ApiResponse<List<Map<String, Object>>> listVoices() {
        return ApiResponse.ok(ttsService.listVoices());
    }

    @PostMapping("/synthesize")
    @Operation(summary = "Synthesize speech")
    public ApiResponse<TtsTask> synthesize(@Valid @RequestBody TtsRequest request) {
        String userId = getCurrentUserId();
        return ApiResponse.ok(ttsService.synthesize(userId, request));
    }

    @PostMapping("/synthesize/stream")
    @Operation(summary = "Stream synthesize speech")
    public SseEmitter synthesizeStream(@Valid @RequestBody TtsRequest request) {
        String userId = getCurrentUserId();
        return ttsService.synthesizeStream(userId, request);
    }

    @GetMapping("/engines")
    @Operation(summary = "Get TTS engine info")
    public ApiResponse<List<Map<String, Object>>> getEngineInfo() {
        return ApiResponse.ok(ttsService.getEngineInfo());
    }

    private String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
