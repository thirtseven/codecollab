package com.codecollab.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiConversationVO {
    private Long id;
    private String title;
    private Long projectId;
    private LocalDateTime createdAt;
    private Integer messageCount;
}
