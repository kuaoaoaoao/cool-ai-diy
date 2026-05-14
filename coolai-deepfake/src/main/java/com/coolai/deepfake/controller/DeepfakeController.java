package com.coolai.deepfake.controller;

import com.coolai.common.model.ApiResponse;
import com.coolai.deepfake.engine.ReplaceEngine;
import com.coolai.deepfake.service.DeepfakeService;
import com.coolai.model.dto.ReplaceRequest;
import com.coolai.model.entity.ReplaceTask;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/deepfake")
@RequiredArgsConstructor
@Tag(name = "Deepfake", description = "Person replacement (DeepFake) APIs")
public class DeepfakeController {

    private final DeepfakeService deepfakeService;

    @PostMapping("/replace")
    @Operation(summary = "Submit face replacement task")
    public ApiResponse<ReplaceTask> replace(@Valid @RequestBody ReplaceRequest request) {
        String userId = getCurrentUserId();
        return ApiResponse.ok(deepfakeService.replaceFace(userId, request));
    }

    @PostMapping("/detect-faces")
    @Operation(summary = "Detect faces in video frame")
    public ApiResponse<List<ReplaceEngine.DetectedFace>> detectFaces(@RequestBody byte[] videoFrame) {
        return ApiResponse.ok(deepfakeService.detectFaces(videoFrame));
    }

    @PostMapping("/agreement")
    @Operation(summary = "Sign deepfake usage agreement")
    public ApiResponse<Void> signAgreement() {
        String userId = getCurrentUserId();
        // agreementService.signAgreement(userId, "DEEPFAKE", "1.0");
        return ApiResponse.ok();
    }

    @GetMapping("/engines")
    @Operation(summary = "Get replace engine info")
    public ApiResponse<List<Map<String, Object>>> getEngineInfo() {
        return ApiResponse.ok(deepfakeService.getEngineInfo());
    }

    private String getCurrentUserId() {
        return (String) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
    }
}
