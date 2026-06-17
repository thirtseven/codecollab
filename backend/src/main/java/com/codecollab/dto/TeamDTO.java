package com.codecollab.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class TeamDTO {
    @NotBlank(message = "Team name is required")
    @Size(min = 2, max = 50, message = "Team name must be 2-50 characters")
    private String name;

    private String description;
    private String avatar;
    private Integer maxMembers;
}
