package com.coolai.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_logs")
public class OperationLog {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    private String action;

    private String resourceType;

    private String resourceId;

    private String details;

    private String ipAddress;

    private LocalDateTime createdAt;
}
