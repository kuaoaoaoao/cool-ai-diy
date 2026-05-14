package com.coolai.video.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * CogVideo Engine - Text-to-video generation (requires GPU)
 */
@Slf4j
@Component
public class CogVideoEngine implements VideoEngine {

    @Value("${coolai.video.cogvideo.enabled:false}")
    private boolean enabled;

    @Value("${coolai.video.cogvideo.script-path:/opt/cogvideo/inference.py}")
    private String scriptPath;

    @Override
    public String getName() {
        return "cog_video";
    }

    @Override
    public boolean isAvailable() {
        return enabled;
    }

    @Override
    public VideoResult generate(VideoEngineRequest request) {
        try {
            String outputPath = "/tmp/cogvideo_" + System.currentTimeMillis() + ".mp4";
            ProcessBuilder pb = new ProcessBuilder(
                    "python3", scriptPath,
                    "--prompt", request.prompt(),
                    "--duration", String.valueOf((int) request.duration()),
                    "--resolution", request.resolution(),
                    "--output", outputPath
            );
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) throw new RuntimeException("CogVideo inference failed");
            return new VideoResult(outputPath, request.duration(), request.resolution(), request.fps());
        } catch (Exception e) {
            throw new RuntimeException("CogVideo generation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public VideoResult stylize(StylizeRequest request) {
        throw new UnsupportedOperationException("CogVideo does not support stylization directly");
    }

    @Override
    public List<String> listStyles() {
        return List.of("realistic", "anime", "cinematic");
    }
}
