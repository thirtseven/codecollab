package com.codecollab.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("projects")
public class Project {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Long teamId;
    private Long creatorId;
    private String language;
    private String repositoryUrl;
    private Integer status;
    private Integer isPublic;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String teamName;
    @TableField(exist = false)
    private String creatorName;
    @TableField(exist = false)
    private Integer fileCount;
    @TableField(exist = false)
    private Integer submissionCount;
}
