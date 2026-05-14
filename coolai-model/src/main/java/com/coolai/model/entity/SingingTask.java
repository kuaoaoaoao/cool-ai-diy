package com.coolai.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("singing_tasks")
public class SingingTask {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    private String lyrics;

    private String voiceModelId;

    private String style;

    private String engine;

    private String status;

    private String outputPath;

    private Float duration;

    private String errorMsg;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}
