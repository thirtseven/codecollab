package com.codecollab.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectVO {
    private Long id;
    private String name;
    private String description;
    private Long teamId;
    private String teamName;
    private Long creatorId;
    private String creatorName;
    private String language;
    private Integer status;
    private Integer isPublic;
    private Integer fileCount;
    private Integer submissionCount;
    private LocalDateTime createdAt;
}
