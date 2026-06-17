package com.codecollab.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("code_submissions")
public class CodeSubmission {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private Long userId;
    private String title;
    private String description;
    private String branch;
    private String status;
    private String reviewComment;
    private Long reviewerId;
    private LocalDateTime reviewedAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String username;
    @TableField(exist = false)
    private String projectName;
    @TableField(exist = false)
    private Integer fileCount;
}
