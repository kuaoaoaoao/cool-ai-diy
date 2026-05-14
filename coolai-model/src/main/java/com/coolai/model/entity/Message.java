package com.coolai.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName(value = "messages", autoResultMap = true)
public class Message {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String convId;

    private String role;

    private String content;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> metadata;

    private Integer tokens;

    private LocalDateTime createdAt;
}
