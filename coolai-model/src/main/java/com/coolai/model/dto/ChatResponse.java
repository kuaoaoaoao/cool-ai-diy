package com.coolai.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private String conversationId;
    private String messageId;
    private String content;
    private String role;
    private Map<String, Object> usage;
    private List<Map<String, Object>> plan;
    private List<Map<String, Object>> stepResults;
}
