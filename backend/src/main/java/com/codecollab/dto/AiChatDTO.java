package com.codecollab.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class AiChatDTO {
    private Long conversationId;

    @NotBlank(message = "Message is required")
    private String message;

    private Long projectId;
}
