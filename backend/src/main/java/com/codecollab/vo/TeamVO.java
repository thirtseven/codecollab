package com.codecollab.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TeamVO {
    private Long id;
    private String teamCode;
    private String name;
    private String description;
    private String avatar;
    private Long creatorId;
    private String creatorName;
    private Integer maxMembers;
    private Integer memberCount;
    private Integer status;
    private String inviteCode;
    private LocalDateTime createdAt;
    private List<TeamMemberVO> members;
}
