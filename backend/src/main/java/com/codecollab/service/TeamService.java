package com.codecollab.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.codecollab.common.PageResult;
import com.codecollab.dto.TeamDTO;
import com.codecollab.entity.Team;
import com.codecollab.vo.TeamMemberVO;
import com.codecollab.vo.TeamVO;
import java.util.List;

public interface TeamService extends IService<Team> {
    TeamVO createTeam(Long userId, TeamDTO dto);
    TeamVO updateTeam(Long teamId, Long userId, TeamDTO dto);
    void deleteTeam(Long teamId, Long userId);
    TeamVO getTeamDetail(Long teamId);
    PageResult<TeamVO> getTeamPage(int page, int size, String keyword);
    List<TeamVO> getUserTeams(Long userId);
    void joinTeam(Long userId, String inviteCode);
    void leaveTeam(Long teamId, Long userId);
    void removeMember(Long teamId, Long operatorId, Long targetUserId);
    void updateMemberRole(Long teamId, Long operatorId, Long targetUserId, String role);
    List<TeamMemberVO> getTeamMembers(Long teamId);
}
