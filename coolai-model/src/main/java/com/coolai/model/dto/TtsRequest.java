package com.coolai.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TtsRequest {

    @NotBlank(message = "Text is required")
    private String text;

    private String voiceId;

    private String engine;

    private String format;

    private Float speed;

    private Float pitch;
}
