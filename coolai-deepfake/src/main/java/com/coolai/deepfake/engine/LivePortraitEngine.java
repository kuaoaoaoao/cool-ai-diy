package com.coolai.deepfake.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * LivePortrait Engine - Photo-driven facial expression and motion transfer
 */
@Slf4j
@Component
public class LivePortraitEngine implements ReplaceEngine {

    @Value("${coolai.deepfake.liveportrait.enabled:false}")
    private boolean enabled;

    @Override
    public String getName() { return "live_portrait"; }

    @Override
    public boolean isAvailable() { return enabled; }

    @Override
    public ReplaceResult replace(ReplaceEngineRequest request) {
        try {
            String outputPath = "/tmp/liveportrait_" + System.currentTimeMillis() + ".mp4";
            ProcessBuilder pb = new ProcessBuilder(
                    "python3", "/opt/liveportrait/inference.py",
                    "--source", request.targetFacePath(),
                    "--driving", request.sourceVideoPath(),
                    "--output", outputPath
            );
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) throw new RuntimeException("LivePortrait failed");
            return new ReplaceResult(outputPath, true);
        } catch (Exception e) {
            throw new RuntimeException("LivePortrait failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DetectedFace> detectFaces(byte[] videoFrame) { return Collections.emptyList(); }
}
