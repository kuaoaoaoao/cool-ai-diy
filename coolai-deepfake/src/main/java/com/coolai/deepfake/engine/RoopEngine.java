package com.coolai.deepfake.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Roop Engine - Simple single-image face swap
 */
@Slf4j
@Component
public class RoopEngine implements ReplaceEngine {

    @Value("${coolai.deepfake.roop.enabled:false}")
    private boolean enabled;

    @Override
    public String getName() { return "roop"; }

    @Override
    public boolean isAvailable() { return enabled; }

    @Override
    public ReplaceResult replace(ReplaceEngineRequest request) {
        try {
            String outputPath = "/tmp/roop_" + System.currentTimeMillis() + ".mp4";
            ProcessBuilder pb = new ProcessBuilder(
                    "python3", "/opt/roop/run.py",
                    "--src", request.targetFacePath(),
                    "--tgt", request.sourceVideoPath(),
                    "--output", outputPath
            );
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) throw new RuntimeException("Roop failed");
            return new ReplaceResult(outputPath, true);
        } catch (Exception e) {
            throw new RuntimeException("Roop failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DetectedFace> detectFaces(byte[] videoFrame) { return Collections.emptyList(); }
}
