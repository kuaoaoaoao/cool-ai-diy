package com.coolai.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class ChatRequest {

    @NotBlank(message = "Message content is required")
    private String content;

    private String conversationId;

    private String model;

    private Boolean useAgent;

    private Boolean useKnowledge;

    private String knowledgeBaseId;

    private Map<String, Object> config;
}
