package com.coolai.tts.service;

import com.coolai.model.dto.TtsRequest;
import com.coolai.model.entity.TtsTask;
import com.coolai.model.enums.TaskStatus;
import com.coolai.tts.engine.TtsEngine;
import com.coolai.tts.mapper.TtsTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TtsService {

    private final TtsTaskMapper ttsTaskMapper;
    private final List<TtsEngine> engines;

    public TtsEngine getEngine(String engineName) {
        return engines.stream()
                .filter(e -> e.getName().equals(engineName) && e.isAvailable())
                .findFirst()
                .orElseGet(() -> engines.stream()
                        .filter(TtsEngine::isAvailable)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No available TTS engine")));
    }

    public TtsTask synthesize(String userId, TtsRequest request) {
        String engineName = request.getEngine() != null ? request.getEngine() : "edge_tts";
        TtsEngine engine = getEngine(engineName);

        // Create task record
        TtsTask task = new TtsTask();
        task.setUserId(userId);
        task.setText(request.getText());
        task.setVoiceId(request.getVoiceId());
        task.setStatus(TaskStatus.PROCESSING.getValue());
        task.setCreatedAt(LocalDateTime.now());
        ttsTaskMapper.insert(task);

        try {
            TtsEngine.TtsResult result = engine.synthesize(request);
            task.setStatus(TaskStatus.COMPLETED.getValue());
            task.setOutputPath(result.filePath());
            task.setCompletedAt(LocalDateTime.now());
        } catch (Exception e) {
            task.setStatus(TaskStatus.FAILED.getValue());
            task.setErrorMsg(e.getMessage());
            task.setCompletedAt(LocalDateTime.now());
            log.error("TTS synthesis failed for task: {}", task.getId(), e);
        }

        ttsTaskMapper.updateById(task);
        return task;
    }

    public SseEmitter synthesizeStream(String userId, TtsRequest request) {
        String engineName = request.getEngine() != null ? request.getEngine() : "edge_tts";
        TtsEngine engine = getEngine(engineName);
        return engine.synthesizeStream(request);
    }

    public List<Map<String, Object>> listVoices() {
        TtsEngine engine = getEngine("edge_tts");
        return engine.listVoices();
    }

    public List<Map<String, Object>> getEngineInfo() {
        return engines.stream()
                .map(e -> Map.<String, Object>of(
                        "name", e.getName(),
                        "available", e.isAvailable()
                ))
                .toList();
    }
}
