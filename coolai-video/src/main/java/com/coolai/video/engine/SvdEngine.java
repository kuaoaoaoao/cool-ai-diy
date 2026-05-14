package com.coolai.video.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Stable Video Diffusion Engine - Image-to-video generation (requires GPU)
 */
@Slf4j
@Component
public class SvdEngine implements VideoEngine {

    @Value("${coolai.video.svd.enabled:false}")
    private boolean enabled;

    @Value("${coolai.video.svd.script-path:/opt/svd/inference.py}")
    private String scriptPath;

    @Override
    public String getName() { return "svd"; }

    @Override
    public boolean isAvailable() { return enabled; }

    @Override
    public VideoResult generate(VideoEngineRequest request) {
        try {
            String outputPath = "/tmp/svd_" + System.currentTimeMillis() + ".mp4";
            ProcessBuilder pb = new ProcessBuilder(
                    "python3", scriptPath,
                    "--prompt", request.prompt(),
                    "--duration", String.valueOf((int) request.duration()),
                    "--output", outputPath
            );
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) throw new RuntimeException("SVD inference failed");
            return new VideoResult(outputPath, request.duration(), request.resolution(), request.fps());
        } catch (Exception e) {
            throw new RuntimeException("SVD generation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public VideoResult stylize(StylizeRequest request) {
        throw new UnsupportedOperationException("SVD does not support stylization directly");
    }

    @Override
    public List<String> listStyles() { return List.of("realistic", "cinematic"); }
}
