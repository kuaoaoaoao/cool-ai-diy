package com.coolai.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tts_tasks")
public class TtsTask {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    private String text;

    private String voiceId;

    private String status;

    private String outputPath;

    private String errorMsg;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}
