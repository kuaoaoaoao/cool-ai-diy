package com.coolai.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName(value = "knowledge_bases", autoResultMap = true)
public class KnowledgeBase {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    private String name;

    private String description;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> config;

    private Integer chunkCount;

    private Integer docCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
