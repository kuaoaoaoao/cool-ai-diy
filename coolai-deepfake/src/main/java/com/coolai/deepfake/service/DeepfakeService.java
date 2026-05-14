package com.coolai.deepfake.service;

import com.coolai.deepfake.engine.ReplaceEngine;
import com.coolai.deepfake.safety.AgreementService;
import com.coolai.deepfake.safety.WatermarkService;
import com.coolai.model.dto.ReplaceRequest;
import com.coolai.model.entity.ReplaceTask;
import com.coolai.model.enums.TaskStatus;
import com.coolai.deepfake.mapper.ReplaceTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeepfakeService {

    private final ReplaceTaskMapper replaceTaskMapper;
    private final List<ReplaceEngine> engines;
    private final WatermarkService watermarkService;
    private final AgreementService agreementService;
    private final RabbitTemplate rabbitTemplate;

    public ReplaceEngine getEngine(String engineName) {
        return engines.stream()
                .filter(e -> e.getName().equals(engineName) && e.isAvailable())
                .findFirst()
                .orElseGet(() -> engines.stream()
                        .filter(ReplaceEngine::isAvailable)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No available replace engine")));
    }

    public ReplaceTask replaceFace(String userId, ReplaceRequest request) {
        // Security check: user must have signed agreement
        if (!agreementService.hasSignedAgreement(userId, "DEEPFAKE")) {
            throw new RuntimeException("User must sign deepfake usage agreement first");
        }

        String engineName = request.getEngine() != null ? request.getEngine() : "mock";
        ReplaceEngine engine = getEngine(engineName);

        ReplaceTask task = new ReplaceTask();
        task.setUserId(userId);
        task.setSourceVideoPath(request.getSourceVideoId());
        task.setTargetFacePath(request.getTargetFaceImage());
        task.setMode(request.getMode() != null ? request.getMode() : "face_swap");
        task.setEngine(engineName);
        task.setHasWatermark(true); // Mandatory
        task.setStatus(TaskStatus.PENDING.getValue());
        task.setCreatedAt(LocalDateTime.now());
        replaceTaskMapper.insert(task);

        // GPU tasks go to queue
        if ("face_fusion".equals(engineName) || "inswapper".equals(engineName) || "live_portrait".equals(engineName)) {
            task.setStatus(TaskStatus.QUEUED.getValue());
            replaceTaskMapper.updateById(task);
            rabbitTemplate.convertAndSend("coolai.exchange", "coolai.deepfake.replace", task);
        } else {
            try {
                ReplaceEngine.ReplaceEngineRequest engineReq = new ReplaceEngine.ReplaceEngineRequest(
                        task.getSourceVideoPath(), task.getTargetFacePath(),
                        request.getTargetFaceIndex() != null ? request.getTargetFaceIndex() : 0,
                        task.getMode(),
                        request.getBlendMode() != null ? request.getBlendMode() : "natural",
                        request.getEnhanceFace() != null ? request.getEnhanceFace() : false,
                        request.getOutputFormat() != null ? request.getOutputFormat() : "mp4"
                );
                ReplaceEngine.ReplaceResult result = engine.replace(engineReq);
                task.setStatus(TaskStatus.COMPLETED.getValue());
                task.setOutputPath(result.filePath());
                task.setHasWatermark(result.hasWatermark());
                task.setCompletedAt(LocalDateTime.now());
            } catch (Exception e) {
                task.setStatus(TaskStatus.FAILED.getValue());
                task.setErrorMsg(e.getMessage());
                task.setCompletedAt(LocalDateTime.now());
            }
            replaceTaskMapper.updateById(task);
        }
        return task;
    }

    public List<ReplaceEngine.DetectedFace> detectFaces(byte[] videoFrame) {
        ReplaceEngine engine = getEngine("mock");
        return engine.detectFaces(videoFrame);
    }

    public List<Map<String, Object>> getEngineInfo() {
        return engines.stream()
                .map(e -> Map.<String, Object>of("name", e.getName(), "available", e.isAvailable()))
                .toList();
    }
}
