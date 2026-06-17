package com.codecollab.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("project_files")
public class ProjectFile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    private String filePath;
    private String fileName;
    private String content;
    private String fileType;
    private Long fileSize;
    private Integer version;
    private Long creatorId;
    private Long parentId;
    private Integer isDirectory;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;
}
