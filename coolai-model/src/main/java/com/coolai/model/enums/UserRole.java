package com.coolai.model.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN("admin"),
    USER("user");

    private final String value;

    UserRole(String value) {
        this.value = value;
    }
}
