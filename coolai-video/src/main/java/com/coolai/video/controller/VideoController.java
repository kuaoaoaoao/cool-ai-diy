package com.coolai.video.controller;

import com.coolai.common.model.ApiResponse;
import com.coolai.model.dto.VideoRequest;
import com.coolai.model.entity.VideoTask;
import com.coolai.video.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/video")
@RequiredArgsConstructor
@Tag(name = "Video", description = "Video generation APIs")
public class VideoController {

    private final VideoService videoService;

    @PostMapping("/generate")
    @Operation(summary = "Generate video")
    public ApiResponse<VideoTask> generate(@Valid @RequestBody VideoRequest request) {
        String userId = getCurrentUserId();
        return ApiResponse.ok(videoService.generate(userId, request));
    }

    @GetMapping("/styles")
    @Operation(summary = "List available video styles")
    public ApiResponse<List<String>> listStyles() {
        return ApiResponse.ok(videoService.getEngine("mock").listStyles());
    }

    @GetMapping("/engines")
    @Operation(summary = "Get video engine info")
    public ApiResponse<List<Map<String, Object>>> getEngineInfo() {
        return ApiResponse.ok(videoService.getEngineInfo());
    }

    private String getCurrentUserId() {
        return (String) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
    }
}
