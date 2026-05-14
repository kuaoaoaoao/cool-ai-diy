package com.coolai.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SingingRequest {

    @NotBlank(message = "Lyrics are required")
    private String lyrics;

    private String melodySource;

    private String voiceModelId;

    private String style;

    private Float tempo;

    private Integer key;

    private String format;

    private String engine;
}
