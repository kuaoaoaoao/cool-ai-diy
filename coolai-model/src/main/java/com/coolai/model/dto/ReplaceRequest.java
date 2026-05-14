package com.coolai.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReplaceRequest {

    @NotBlank(message = "Source video ID is required")
    private String sourceVideoId;

    private String targetFaceImage;

    private Integer targetFaceIndex;

    private String mode;

    private String blendMode;

    private Boolean enhanceFace;

    private String outputFormat;

    private Boolean watermark;

    private String engine;
}
