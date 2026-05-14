package com.coolai.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("voice_models")
public class VoiceModel {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    private String name;

    private String description;

    private String modelPath;

    private String refAudio;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
