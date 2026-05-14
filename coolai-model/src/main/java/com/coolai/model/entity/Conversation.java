package com.coolai.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName(value = "conversations", autoResultMap = true)
public class Conversation {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    private String title;

    private String model;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> config;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
