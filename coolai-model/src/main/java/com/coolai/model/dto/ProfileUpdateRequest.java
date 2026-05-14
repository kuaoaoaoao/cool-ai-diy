package com.coolai.model.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ProfileUpdateRequest {

    private String username;
    private String email;
    private Map<String, Object> config;
}
