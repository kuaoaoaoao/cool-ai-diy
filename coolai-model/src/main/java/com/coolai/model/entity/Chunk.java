package com.coolai.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@TableName(value = "chunks", autoResultMap = true)
public class Chunk {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String docId;

    private String content;

    private Integer chunkIndex;

    private String vectorId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> metadata;

    private LocalDateTime createdAt;
}
