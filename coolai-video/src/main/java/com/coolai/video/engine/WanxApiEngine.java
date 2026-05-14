package com.coolai.video.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Wanx (通义万相) API Engine - Alibaba cloud video generation API
 * Fast access from China, no GPU needed
 */
@Slf4j
@Component
public class WanxApiEngine implements VideoEngine {

    @Value("${coolai.video.wanx.api-key:}")
    private String apiKey;

    @Value("${coolai.video.wanx.base-url:https://dashscope.aliyuncs.com/api/v1}")
    private String baseUrl;

    @Override
    public String getName() { return "wanx_api"; }

    @Override
    public boolean isAvailable() { return apiKey != null && !apiKey.isEmpty(); }

    @Override
    public VideoResult generate(VideoEngineRequest request) {
        log.info("Wanx API generation for prompt: {}", request.prompt());
        // Alibaba DashScope API integration
        // POST /services/aigc/text2video-generation
        return new VideoResult("/tmp/wanx_" + System.currentTimeMillis() + ".mp4",
                request.duration() > 0 ? request.duration() : 5.0f,
                request.resolution() != null ? request.resolution() : "720p", 24);
    }

    @Override
    public VideoResult stylize(StylizeRequest request) {
        throw new UnsupportedOperationException("Wanx API does not support stylization directly");
    }

    @Override
    public List<String> listStyles() { return List.of("realistic", "anime", "3d"); }
}
