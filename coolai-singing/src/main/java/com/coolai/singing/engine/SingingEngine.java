package com.coolai.singing.engine;

import java.util.List;
import java.util.Map;

/**
 * Singing Engine interface - Strategy pattern for multiple singing backends
 */
public interface SingingEngine {

    String getName();

    boolean isAvailable();

    SingingResult synthesize(SingingRequest request);

    List<Map<String, String>> listStyles();

    record SingingRequest(
            String lyrics,
            String voiceModelId,
            String style,
            String melodySource,
            byte[] melodyData,
            float tempo,
            int keyShift,
            String format
    ) {}

    record SingingResult(
            byte[] audioData,
            String format,
            float duration,
            String filePath
    ) {}
}
