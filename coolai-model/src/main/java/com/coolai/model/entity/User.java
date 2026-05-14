package com.coolai.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName(value = "users", autoResultMap = true)
public class User {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String username;

    private String email;

    private String passwordHash;

    private String role;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> config;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
