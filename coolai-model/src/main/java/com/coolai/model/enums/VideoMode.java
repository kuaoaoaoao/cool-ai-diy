package com.coolai.model.enums;

import lombok.Getter;

@Getter
public enum VideoMode {
    TEXT2VID("text2vid"),
    IMG2VID("img2vid"),
    STYLE_TRANSFER("style_transfer");

    private final String value;

    VideoMode(String value) {
        this.value = value;
    }
}
