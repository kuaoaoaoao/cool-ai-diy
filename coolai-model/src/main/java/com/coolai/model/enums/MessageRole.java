package com.coolai.model.enums;

import lombok.Getter;

@Getter
public enum MessageRole {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant");

    private final String value;

    MessageRole(String value) {
        this.value = value;
    }
}
