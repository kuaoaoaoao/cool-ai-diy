package com.coolai.video.service;

import com.coolai.model.dto.VideoRequest;
import com.coolai.model.entity.VideoTask;
import com.coolai.model.enums.TaskStatus;
import com.coolai.video.engine.VideoEngine;
import com.coolai.video.mapper.VideoTaskMapper;
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
public class VideoService {

    private final VideoTaskMapper videoTaskMapper;
    private final List<VideoEngine> engines;
    private final RabbitTemplate rabbitTemplate;

    public VideoEngine getEngine(String engineName) {
        return engines.stream()
                .filter(e -> e.getName().equals(engineName) && e.isAvailable())
                .findFirst()
                .orElseGet(() -> engines.stream()
                        .filter(VideoEngine::isAvailable)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No available video engine")));
    }

    public VideoTask generate(String userId, VideoRequest request) {
        String engineName = request.getEngine() != null ? request.getEngine() : "mock";
        VideoEngine engine = getEngine(engineName);

        VideoTask task = new VideoTask();
        task.setUserId(userId);
        task.setPrompt(request.getPrompt());
        task.setMode(request.getMode() != null ? request.getMode() : "text2vid");
        task.setEngine(engineName);
        task.setResolution(request.getResolution() != null ? request.getResolution() : "720p");
        task.setFps(request.getFps() != null ? request.getFps() : 24);
        task.setStatus(TaskStatus.PENDING.getValue());
        task.setCreatedAt(LocalDateTime.now());
        videoTaskMapper.insert(task);

        // GPU tasks go to queue
        if ("cog_video".equals(engineName) || "svd".equals(engineName)) {
            task.setStatus(TaskStatus.QUEUED.getValue());
            videoTaskMapper.updateById(task);
            rabbitTemplate.convertAndSend("coolai.exchange", "coolai.video.generate", task);
        } else {
            try {
                VideoEngine.VideoEngineRequest engineReq = new VideoEngine.VideoEngineRequest(
                        request.getPrompt(), request.getMode(), null,
                        request.getDuration() != null ? request.getDuration() : 5.0f,
                        task.getResolution(), task.getFps(),
                        request.getStyle(), request.getNegativePrompt(), request.getSeed()
                );
                VideoEngine.VideoResult result = engine.generate(engineReq);
                task.setStatus(TaskStatus.COMPLETED.getValue());
                task.setOutputPath(result.filePath());
                task.setDuration(result.duration());
                task.setCompletedAt(LocalDateTime.now());
            } catch (Exception e) {
                task.setStatus(TaskStatus.FAILED.getValue());
                task.setErrorMsg(e.getMessage());
                task.setCompletedAt(LocalDateTime.now());
            }
            videoTaskMapper.updateById(task);
        }
        return task;
    }

    public List<Map<String, Object>> getEngineInfo() {
        return engines.stream()
                .map(e -> Map.<String, Object>of("name", e.getName(), "available", e.isAvailable()))
                .toList();
    }
}
