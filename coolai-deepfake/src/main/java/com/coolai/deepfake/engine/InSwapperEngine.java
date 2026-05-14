package com.coolai.deepfake.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * InSwapper Engine - Lightweight face swap using InsightFace inswapper
 */
@Slf4j
@Component
public class InSwapperEngine implements ReplaceEngine {

    @Value("${coolai.deepfake.inswapper.enabled:false}")
    private boolean enabled;

    @Override
    public String getName() { return "inswapper"; }

    @Override
    public boolean isAvailable() { return enabled; }

    @Override
    public ReplaceResult replace(ReplaceEngineRequest request) {
        try {
            String outputPath = "/tmp/inswapper_" + System.currentTimeMillis() + ".mp4";
            ProcessBuilder pb = new ProcessBuilder(
                    "python3", "/opt/inswapper/inference.py",
                    "--source", request.targetFacePath(),
                    "--target", request.sourceVideoPath(),
                    "--output", outputPath
            );
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) throw new RuntimeException("InSwapper failed");
            return new ReplaceResult(outputPath, true);
        } catch (Exception e) {
            throw new RuntimeException("InSwapper failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DetectedFace> detectFaces(byte[] videoFrame) { return Collections.emptyList(); }
}
