package com.coolai.voice.service;

import com.coolai.voice.engine.CloneEngine;
import com.coolai.voice.mapper.VoiceModelMapper;
import com.coolai.model.entity.VoiceModel;
import com.coolai.model.enums.TaskStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceCloneService {

    private final VoiceModelMapper voiceModelMapper;
    private final List<CloneEngine> engines;

    public CloneEngine getEngine(String engineName) {
        return engines.stream()
                .filter(e -> e.getName().equals(engineName) && e.isAvailable())
                .findFirst()
                .orElseGet(() -> engines.stream()
                        .filter(CloneEngine::isAvailable)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("No available clone engine")));
    }

    public VoiceModel createModel(String userId, String name, String description) {
        VoiceModel model = new VoiceModel();
        model.setUserId(userId);
        model.setName(name);
        model.setDescription(description);
        model.setStatus(TaskStatus.PENDING.getValue());
        model.setCreatedAt(LocalDateTime.now());
        model.setUpdatedAt(LocalDateTime.now());
        voiceModelMapper.insert(model);
        return model;
    }

    public CloneEngine.CloneResult synthesize(String userId, String text, String voiceModelId, String engineName) {
        VoiceModel voiceModel = voiceModelMapper.selectById(voiceModelId);
        if (voiceModel == null) {
            throw new RuntimeException("Voice model not found: " + voiceModelId);
        }

        CloneEngine engine = getEngine(engineName);
        CloneEngine.CloneRequest request = new CloneEngine.CloneRequest(
                text, voiceModelId, voiceModel.getRefAudio(), "mp3"
        );
        return engine.synthesize(request);
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
