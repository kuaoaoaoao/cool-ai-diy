package com.coolai.tts.engine;

import com.coolai.model.dto.TtsRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

/**
 * TTS Engine interface - Strategy pattern for multiple TTS backends
 */
public interface TtsEngine {

    String getName();

    boolean isAvailable();

    TtsResult synthesize(TtsRequest request);

    SseEmitter synthesizeStream(TtsRequest request);

    List<Map<String, Object>> listVoices();

    record TtsResult(
            byte[] audioData,
            String format,
            long duration,
            String filePath
    ) {}
}
