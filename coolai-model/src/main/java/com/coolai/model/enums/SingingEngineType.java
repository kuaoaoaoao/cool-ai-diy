package com.coolai.model.enums;

import lombok.Getter;

@Getter
public enum SingingEngineType {
    RVC("rvc"),
    DIFF_SINGER("diff_singer"),
    SVC("svc"),
    EDGE_TTS("edge_tts");

    private final String value;

    SingingEngineType(String value) {
        this.value = value;
    }
}
