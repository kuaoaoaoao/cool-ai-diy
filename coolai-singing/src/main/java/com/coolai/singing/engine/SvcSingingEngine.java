package com.coolai.singing.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * So-VITS-SVC Engine - Voice conversion for singing (requires training + GPU)
 */
@Slf4j
@Component
public class SvcSingingEngine implements SingingEngine {

    @Value("${coolai.singing.svc.enabled:false}")
    private boolean enabled;

    @Value("${coolai.singing.svc.script-path:/opt/svc/inference.py}")
    private String scriptPath;

    @Override
    public String getName() { return "svc"; }

    @Override
    public boolean isAvailable() { return enabled; }

    @Override
    public SingingResult synthesize(SingingRequest request) {
        try {
            String outputPath = "/tmp/singing_svc_" + System.currentTimeMillis() + ".wav";
            ProcessBuilder pb = new ProcessBuilder(
                    "python3", scriptPath,
                    "--lyrics", request.lyrics(),
                    "--voice-model", request.voiceModelId(),
                    "--output", outputPath
            );
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) throw new RuntimeException("SVC inference failed");
            return new SingingResult(null, "wav", 0, outputPath);
        } catch (Exception e) {
            log.error("SVC synthesis failed", e);
            throw new RuntimeException("SVC failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Map<String, String>> listStyles() {
        return List.of(Map.of("id", "default", "name", "Default (requires trained model)"));
    }
}
