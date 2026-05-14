package com.coolai.video.engine;

import java.util.List;
import java.util.Map;

/**
 * Video Engine interface - Strategy pattern for multiple video generation backends
 */
public interface VideoEngine {

    String getName();

    boolean isAvailable();

    VideoResult generate(VideoEngineRequest request);

    VideoResult stylize(StylizeRequest request);

    List<String> listStyles();

    record VideoEngineRequest(
            String prompt,
            String mode,
            byte[] referenceImage,
            float duration,
            String resolution,
            int fps,
            String style,
            String negativePrompt,
            Long seed
    ) {}

    record StylizeRequest(
            String sourceVideoPath,
            String style,
            String prompt
    ) {}

    record VideoResult(
            String filePath,
            float duration,
            String resolution,
            int fps
    ) {}
}
