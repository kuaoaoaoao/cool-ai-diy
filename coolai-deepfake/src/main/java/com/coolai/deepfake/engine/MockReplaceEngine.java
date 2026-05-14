package com.coolai.deepfake.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mock Replace Engine - For development and testing
 */
@Slf4j
@Component
public class MockReplaceEngine implements ReplaceEngine {

    @Override
    public String getName() {
        return "mock";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public ReplaceResult replace(ReplaceEngineRequest request) {
        log.info("Mock face replacement for source: {}", request.sourceVideoPath());
        return new ReplaceResult("/tmp/mock_replace_" + System.currentTimeMillis() + ".mp4", true);
    }

    @Override
    public List<DetectedFace> detectFaces(byte[] videoFrame) {
        return List.of(new DetectedFace(0, 100, 100, 200, 200, 0.95f));
    }
}
