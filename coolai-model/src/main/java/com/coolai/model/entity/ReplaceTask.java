package com.coolai.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("replace_tasks")
public class ReplaceTask {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    private String sourceVideoPath;

    private String targetFacePath;

    private String mode;

    private String engine;

    private String status;

    private String outputPath;

    private Boolean hasWatermark;

    private String errorMsg;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}
