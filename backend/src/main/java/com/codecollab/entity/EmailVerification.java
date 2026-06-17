package com.codecollab.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("email_verifications")
public class EmailVerification {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String email;
    private String code;
    private String type;
    private LocalDateTime expireTime;
    private Integer used;
    private LocalDateTime createdAt;
}
