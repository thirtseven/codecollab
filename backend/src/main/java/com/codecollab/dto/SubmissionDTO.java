package com.codecollab.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class SubmissionDTO {
    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String branch;
    private List<SubmissionFileDTO> files;

    @Data
    public static class SubmissionFileDTO {
        @NotBlank(message = "File path is required")
        private String filePath;
        private String fileName;
        private String content;
        private String diffContent;
        private String changeType;
    }
}
