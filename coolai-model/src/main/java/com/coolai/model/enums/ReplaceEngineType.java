package com.coolai.model.enums;

import lombok.Getter;

@Getter
public enum ReplaceEngineType {
    FACE_FUSION("face_fusion"),
    INSWAPPER("inswapper"),
    LIVE_PORTRAIT("live_portrait"),
    ROOP("roop"),
    MOCK("mock");

    private final String value;

    ReplaceEngineType(String value) {
        this.value = value;
    }
}
