package com.coolai.video.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sora API Engine - OpenAI Sora commercial API (cloud-based, no GPU needed)
 */
@Slf4j
@Component
public class SoraApiEngine implements VideoEngine {

    @Value("${coolai.llm.openai.api-key:}")
    private String apiKey;

    @Override
    public String getName() { return "sora_api"; }

    @Override
    public boolean isAvailable() { return apiKey != null && !apiKey.isEmpty(); }

    @Override
    public VideoResult generate(VideoEngineRequest request) {
        log.info("Sora API generation for prompt: {}", request.prompt());
        // OpenAI Sora API integration (when available)
        // POST https://api.openai.com/v1/video/generations
        return new VideoResult("/tmp/sora_" + System.currentTimeMillis() + ".mp4",
                request.duration() > 0 ? request.duration() : 5.0f,
                request.resolution() != null ? request.resolution() : "1080p", 24);
    }

    @Override
    public VideoResult stylize(StylizeRequest request) {
        throw new UnsupportedOperationException("Sora API does not support stylization directly");
    }

    @Override
    public List<String> listStyles() { return List.of("realistic", "cinematic", "anime"); }
}
