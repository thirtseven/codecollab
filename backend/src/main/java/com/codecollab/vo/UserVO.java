package com.codecollab.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private String nickname;
    private String phone;
    private String bio;
    private String role;
    private Integer status;
    private String githubUrl;
    private String website;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createdAt;
}
