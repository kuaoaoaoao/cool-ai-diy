package com.coolai.tts.engine;

import com.coolai.model.dto.TtsRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * CosyVoice Engine - ProcessBuilder calling Python CosyVoice inference script
 */
@Slf4j
@Component
public class CosyVoiceEngine implements TtsEngine {

    @Value("${coolai.tts.cosyvoice.enabled:false}")
    private boolean enabled;

    @Value("${coolai.tts.cosyvoice.script-path:/opt/cosyvoice/inference.py}")
    private String scriptPath;

    @Override
    public String getName() { return "cosy_voice"; }

    @Override
    public boolean isAvailable() { return enabled; }

    @Override
    public TtsResult synthesize(TtsRequest request) {
        try {
            String outputPath = "/tmp/tts_cosyvoice_" + System.currentTimeMillis() + ".wav";
            ProcessBuilder pb = new ProcessBuilder(
                    "python3", scriptPath,
                    "--text", request.getText(),
                    "--output", outputPath
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) throw new RuntimeException("CosyVoice inference failed");
            return new TtsResult(null, "wav", 0, outputPath);
        } catch (Exception e) {
            log.error("CosyVoice synthesis failed", e);
            throw new RuntimeException("CosyVoice failed: " + e.getMessage(), e);
        }
    }

    @Override
    public SseEmitter synthesizeStream(TtsRequest request) {
        SseEmitter emitter = new SseEmitter(120_000L);
        try {
            TtsResult result = synthesize(request);
            emitter.send(SseEmitter.event().data(Map.of("type", "done", "filePath", result.filePath())));
            emitter.complete();
        } catch (Exception e) { emitter.completeWithError(e); }
        return emitter;
    }

    @Override
    public List<Map<String, Object>> listVoices() {
        return List.of(Map.of("id", "cosyvoice_default", "name", "CosyVoice Default", "language", "zh"));
    }
}
