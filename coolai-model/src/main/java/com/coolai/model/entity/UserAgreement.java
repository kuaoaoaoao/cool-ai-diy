package com.coolai.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_agreements")
public class UserAgreement {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String userId;

    private String agreementType;

    private String version;

    private LocalDateTime signedAt;
}
