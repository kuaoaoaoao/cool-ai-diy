package com.coolai.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("video_tasks")
public class VideoTask {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    private String prompt;

    private String mode;

    private String engine;

    private String status;

    private String outputPath;

    private Float duration;

    private String resolution;

    private Integer fps;

    private String errorMsg;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}
