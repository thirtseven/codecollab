package com.codecollab.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SubmissionVO {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long userId;
    private String username;
    private String title;
    private String description;
    private String branch;
    private String status;
    private String reviewComment;
    private Integer fileCount;
    private LocalDateTime createdAt;
}
