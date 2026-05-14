package com.coolai.voice.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * XTTS-v2 Engine - Zero-shot voice cloning
 * Requires GPU, calls Python script via ProcessBuilder
 */
@Slf4j
@Component
public class XttsV2Engine implements CloneEngine {

    @Value("${coolai.voice.xtts.enabled:false}")
    private boolean enabled;

    @Value("${coolai.voice.xtts.script-path:/opt/xtts/inference.py}")
    private String scriptPath;

    @Value("${coolai.voice.xtts.python-path:python3}")
    private String pythonPath;

    @Override
    public String getName() {
        return "xtts_v2";
    }

    @Override
    public boolean isAvailable() {
        return enabled;
    }

    @Override
    public CloneResult synthesize(CloneRequest request) {
        try {
            String outputPath = "/tmp/xtts_" + System.currentTimeMillis() + ".wav";
            ProcessBuilder pb = new ProcessBuilder(
                    pythonPath, scriptPath,
                    "--text", request.text(),
                    "--ref-audio", request.refAudioPath(),
                    "--output", outputPath
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                throw new RuntimeException("XTTS-v2 inference failed with exit code: " + exitCode);
            }

            return new CloneResult(null, "wav", outputPath);
        } catch (Exception e) {
            log.error("XTTS-v2 synthesis failed", e);
            throw new RuntimeException("XTTS-v2 synthesis failed: " + e.getMessage(), e);
        }
    }
}
