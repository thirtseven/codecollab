package com.codecollab.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("submission_files")
public class SubmissionFile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long submissionId;
    private String filePath;
    private String fileName;
    private String content;
    private String diffContent;
    private String changeType;
    private Long fileSize;
}
