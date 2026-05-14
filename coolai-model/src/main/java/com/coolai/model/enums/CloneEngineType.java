package com.coolai.model.enums;

import lombok.Getter;

@Getter
public enum CloneEngineType {
    XTTS_V2("xtts_v2"),
    EDGE_SIMULATE("edge_simulate");

    private final String value;

    CloneEngineType(String value) {
        this.value = value;
    }
}
