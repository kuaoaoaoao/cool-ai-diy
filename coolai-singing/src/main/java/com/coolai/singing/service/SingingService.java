package com.coolai.singing.service;

import com.coolai.model.dto.SingingRequest;
import com.coolai.model.entity.SingingTask;
import com.coolai.model.enums.TaskStatus;
import com.coolai.singing.engine.SingingEngine;
import com.coolai.singing.mapper.SingingTaskMapper;
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
public class SingingService {

    private final SingingTaskMapper singingTaskMapper;
    private final List<SingingEngine> engines;
    private final RabbitTemplate rabbitTemplate;

    public SingingEngine getEngine(String engineName) {
        return engines.stream()
                .filter(e -> e.getName().equals(engineName) && e.isAvailable())
                .findFirst()
                .orElseGet(() -> engines.stream()
                        .filter(SingingEngine::isAvailable)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No available singing engine")));
    }

    public SingingTask synthesize(String userId, SingingRequest request) {
        String engineName = request.getEngine() != null ? request.getEngine() : "edge_tts";
        SingingEngine engine = getEngine(engineName);

        SingingTask task = new SingingTask();
        task.setUserId(userId);
        task.setLyrics(request.getLyrics());
        task.setVoiceModelId(request.getVoiceModelId());
        task.setStyle(request.getStyle());
        task.setEngine(engineName);
        task.setStatus(TaskStatus.PENDING.getValue());
        task.setCreatedAt(LocalDateTime.now());
        singingTaskMapper.insert(task);

        // Send to GPU queue if using GPU engine
        if ("rvc".equals(engineName) || "diff_singer".equals(engineName) || "svc".equals(engineName)) {
            task.setStatus(TaskStatus.QUEUED.getValue());
            singingTaskMapper.updateById(task);
            rabbitTemplate.convertAndSend("coolai.exchange", "coolai.singing.synthesize", task);
        } else {
            // Execute directly for non-GPU engines
            try {
                SingingEngine.SingingRequest engineReq = new SingingEngine.SingingRequest(
                        request.getLyrics(), request.getVoiceModelId(),
                        request.getStyle(), null, null,
                        request.getTempo() != null ? request.getTempo() : 120f,
                        request.getKey() != null ? request.getKey() : 0,
                        request.getFormat() != null ? request.getFormat() : "mp3"
                );
                SingingEngine.SingingResult result = engine.synthesize(engineReq);
                task.setStatus(TaskStatus.COMPLETED.getValue());
                task.setOutputPath(result.filePath());
                task.setDuration(result.duration());
                task.setCompletedAt(LocalDateTime.now());
            } catch (Exception e) {
                task.setStatus(TaskStatus.FAILED.getValue());
                task.setErrorMsg(e.getMessage());
                task.setCompletedAt(LocalDateTime.now());
            }
            singingTaskMapper.updateById(task);
        }

        return task;
    }

    public List<Map<String, Object>> getEngineInfo() {
        return engines.stream()
                .map(e -> Map.<String, Object>of("name", e.getName(), "available", e.isAvailable()))
                .toList();
    }
}
