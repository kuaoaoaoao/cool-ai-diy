package com.coolai.tts.engine;

import com.coolai.model.dto.TtsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;

/**
 * Edge-TTS Engine - Free, high-quality TTS using Microsoft Edge TTS
 */
@Slf4j
@Component
public class EdgeTtsEngine implements TtsEngine {

    @Value("${coolai.tts.edge-tts.enabled:true}")
    private boolean enabled;

    @Override
    public String getName() {
        return "edge_tts";
    }

    @Override
    public boolean isAvailable() {
        return enabled;
    }

    @Override
    public TtsResult synthesize(TtsRequest request) {
        // Edge-TTS implementation via ProcessBuilder calling Python edge-tts
        try {
            String voiceId = request.getVoiceId() != null ? request.getVoiceId() : "zh-CN-XiaoxiaoNeural";
            String format = request.getFormat() != null ? request.getFormat() : "mp3";

            // Use ProcessBuilder to call edge-tts Python command
            String outputPath = "/tmp/tts_" + UUID.randomUUID() + "." + format;
            ProcessBuilder pb = new ProcessBuilder(
                    "edge-tts",
                    "--voice", voiceId,
                    "--text", request.getText(),
                    "--write-media", outputPath
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("Edge-TTS process failed with exit code: " + exitCode);
            }

            return new TtsResult(null, format, 0, outputPath);
        } catch (Exception e) {
            log.error("Edge-TTS synthesis failed", e);
            throw new RuntimeException("Edge-TTS synthesis failed: " + e.getMessage(), e);
        }
    }

    @Override
    public SseEmitter synthesizeStream(TtsRequest request) {
        SseEmitter emitter = new SseEmitter(120_000L);
        // Streaming implementation would use edge-tts --pipe-stdout
        try {
            emitter.send(SseEmitter.event().data(Map.of("type", "start")));
            TtsResult result = synthesize(request);
            emitter.send(SseEmitter.event().data(Map.of("type", "done", "filePath", result.filePath())));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    @Override
    public List<Map<String, Object>> listVoices() {
        // Common Chinese and English voices
        return List.of(
                Map.of("id", "zh-CN-XiaoxiaoNeural", "name", "Xiaoxiao (Female)", "language", "zh-CN"),
                Map.of("id", "zh-CN-YunxiNeural", "name", "Yunxi (Male)", "language", "zh-CN"),
                Map.of("id", "zh-CN-XiaoyiNeural", "name", "Xiaoyi (Female)", "language", "zh-CN"),
                Map.of("id", "en-US-JennyNeural", "name", "Jenny (Female)", "language", "en-US"),
                Map.of("id", "en-US-GuyNeural", "name", "Guy (Male)", "language", "en-US")
        );
    }
}
