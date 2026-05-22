package com.coolai.model.enums;

import lombok.Getter;

@Getter
public enum TtsEngineType {
    MIMO_V25_TTS("mimo_v25_tts"),
    EDGE_TTS("edge_tts"),
    OPENAI_TTS("openai_tts"),
    COSY_VOICE("cosy_voice");

    private final String value;

    TtsEngineType(String value) {
        this.value = value;
    }
}
