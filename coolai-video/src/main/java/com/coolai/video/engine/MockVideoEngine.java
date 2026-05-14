package com.coolai.video.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mock Video Engine - For development and testing
 */
@Slf4j
@Component
public class MockVideoEngine implements VideoEngine {

    @Override
    public String getName() {
        return "mock";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public VideoResult generate(VideoEngineRequest request) {
        log.info("Mock video generation for prompt: {}", request.prompt());
        return new VideoResult("/tmp/mock_video_" + System.currentTimeMillis() + ".mp4",
                request.duration() > 0 ? request.duration() : 5.0f,
                request.resolution() != null ? request.resolution() : "720p",
                request.fps() > 0 ? request.fps() : 24);
    }

    @Override
    public VideoResult stylize(StylizeRequest request) {
        log.info("Mock video stylization for style: {}", request.style());
        return new VideoResult("/tmp/mock_stylized_" + System.currentTimeMillis() + ".mp4",
                5.0f, "720p", 24);
    }

    @Override
    public List<String> listStyles() {
        return List.of("realistic", "anime", "cinematic", "cyberpunk", "oil_painting");
    }
}
