package com.coolai.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VideoRequest {

    @NotBlank(message = "Prompt is required")
    private String prompt;

    private String mode;

    private Float duration;

    private String resolution;

    private Integer fps;

    private String style;

    private String negativePrompt;

    private Long seed;

    private String engine;
}
