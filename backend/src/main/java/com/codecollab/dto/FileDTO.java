package com.codecollab.dto;

import lombok.Data;

@Data
public class FileDTO {
    private String filePath;
    private String fileName;
    private String content;
    private String fileType;
    private Long parentId;
    private Integer isDirectory;
}
