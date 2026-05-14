package com.coolai.model.enums;

import lombok.Getter;

@Getter
public enum VideoEngineType {
    COG_VIDEO("cog_video"),
    SVD("svd"),
    SORA_API("sora_api"),
    WANX_API("wanx_api"),
    MOCK("mock");

    private final String value;

    VideoEngineType(String value) {
        this.value = value;
    }
}
