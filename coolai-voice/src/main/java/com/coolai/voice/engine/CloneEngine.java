package com.coolai.voice.engine;

/**
 * Voice Clone Engine interface
 */
public interface CloneEngine {

    String getName();

    boolean isAvailable();

    CloneResult synthesize(CloneRequest request);

    record CloneRequest(
            String text,
            String voiceModelId,
            String refAudioPath,
            String format
    ) {}

    record CloneResult(
            byte[] audioData,
            String format,
            String filePath
    ) {}
}
