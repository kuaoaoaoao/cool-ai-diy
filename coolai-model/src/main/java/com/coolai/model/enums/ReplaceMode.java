package com.coolai.model.enums;

import lombok.Getter;

@Getter
public enum ReplaceMode {
    FACE_SWAP("face_swap"),
    EXPRESSION_DRIVE("expression_drive");

    private final String value;

    ReplaceMode(String value) {
        this.value = value;
    }
}
