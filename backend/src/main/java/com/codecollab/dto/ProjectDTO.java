package com.codecollab.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class ProjectDTO {
    @NotBlank(message = "Project name is required")
    private String name;

    private String description;

    private Long teamId;

    private String language;
    private Integer isPublic;
}
