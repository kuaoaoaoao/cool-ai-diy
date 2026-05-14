package com.coolai.model.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ApiKeyRequest {

    private Map<String, String> apiKeys;
}
