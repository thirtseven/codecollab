package com.codecollab.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("permissions")
public class Permission {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String role;
    private String resource;
    private String action;
    private String description;
}
