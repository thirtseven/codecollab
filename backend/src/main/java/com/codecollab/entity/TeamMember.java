package com.codecollab.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("team_members")
public class TeamMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long teamId;
    private Long userId;
    private String role;
    private LocalDateTime joinedAt;

    @TableField(exist = false)
    private String username;
    @TableField(exist = false)
    private String avatar;
    @TableField(exist = false)
    private String email;
}
