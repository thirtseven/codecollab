package com.codecollab.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeamMemberVO {
    private Long id;
    private Long userId;
    private String username;
    private String avatar;
    private String email;
    private String role;
    private LocalDateTime joinedAt;
}
