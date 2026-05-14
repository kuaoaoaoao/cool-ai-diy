package com.coolai.deepfake.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * FaceFusion Engine - Recommended face swap engine using InsightFace + GFPGAN
 */
@Slf4j
@Component
public class FaceFusionEngine implements ReplaceEngine {

    @Value("${coolai.deepfake.facefusion.enabled:false}")
    private boolean enabled;

    @Value("${coolai.deepfake.facefusion.script-path:/opt/facefusion/run.py}")
    private String scriptPath;

    @Override
    public String getName() { return "face_fusion"; }

    @Override
    public boolean isAvailable() { return enabled; }

    @Override
    public ReplaceResult replace(ReplaceEngineRequest request) {
        try {
            String outputPath = "/tmp/fusion_" + System.currentTimeMillis() + ".mp4";
            ProcessBuilder pb = new ProcessBuilder(
                    "python3", scriptPath,
                    "--source", request.targetFacePath(),
                    "--target", request.sourceVideoPath(),
                    "--output", outputPath
            );
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) throw new RuntimeException("FaceFusion failed");
            return new ReplaceResult(outputPath, true);
        } catch (Exception e) {
            throw new RuntimeException("FaceFusion failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DetectedFace> detectFaces(byte[] videoFrame) {
        // Use InsightFace for face detection
        return Collections.emptyList();
    }
}
