package com.coolai.model.enums;

import lombok.Getter;

@Getter
public enum TaskStatus {
    PENDING("pending"),
    QUEUED("queued"),
    PROCESSING("processing"),
    COMPLETED("completed"),
    FAILED("failed");

    private final String value;

    TaskStatus(String value) {
        this.value = value;
    }
}
