package com.codecollab.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("teams")
public class Team {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String teamCode;
    private String name;
    private String description;
    private String avatar;
    private Long creatorId;
    private Integer maxMembers;
    private Integer status;
    private String inviteCode;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String creatorName;
    @TableField(exist = false)
    private Integer memberCount;
}
