package com.codecollab.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("system_configs")
public class SystemConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String configKey;
    private String configValue;
    private String description;
    private String type;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
