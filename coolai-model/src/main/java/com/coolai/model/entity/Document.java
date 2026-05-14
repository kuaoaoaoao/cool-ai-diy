package com.coolai.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("documents")
public class Document {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String kbId;

    private String filename;

    private String filePath;

    private Long fileSize;

    private String fileType;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime processedAt;
}
