package com.codecollab.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.codecollab.common.Constants;
import com.codecollab.common.PageResult;
import com.codecollab.dto.TeamDTO;
import com.codecollab.entity.Team;
import com.codecollab.entity.TeamMember;
import com.codecollab.exception.BusinessException;
import com.codecollab.mapper.TeamMapper;
import com.codecollab.mapper.TeamMemberMapper;
import com.codecollab.service.TeamService;
import com.codecollab.vo.TeamMemberVO;
import com.codecollab.vo.TeamVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<TeamMapper, Team> implements TeamService {

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private TeamMemberMapper teamMemberMapper;

    @Override
    @Transactional
    public TeamVO createTeam(Long userId, TeamDTO dto) {
        Team team = new Team();
        team.setName(dto.getName());
        team.setDescription(dto.getDescription());
        team.setAvatar(dto.getAvatar());
        team.setMaxMembers(dto.getMaxMembers());
        team.setCreatorId(userId);
        team.setInviteCode(RandomUtil.randomString(8));
        team.setStatus(Constants.STATUS_ACTIVE);
        teamMapper.insert(team);

        team.setTeamCode(String.format("T%04d", team.getId()));
        teamMapper.updateById(team);

        TeamMember member = new TeamMember();
        member.setTeamId(team.getId());
        member.setUserId(userId);
        member.setRole("OWNER");
        teamMemberMapper.insert(member);

        return getTeamDetail(team.getId(), userId, null);
    }

    @Override
    @Transactional
    public TeamVO updateTeam(Long teamId, Long userId, TeamDTO dto) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException(404, "团队不存在");
        }
        TeamMember member = teamMemberMapper.selectByTeamAndUser(teamId, userId);
        if (member == null || (!member.getRole().equals("OWNER") && !member.getRole().equals("ADMIN"))) {
            throw new BusinessException(403, "无权限更新团队");
        }
        if (dto.getName() != null) team.setName(dto.getName());
        if (dto.getDescription() != null) team.setDescription(dto.getDescription());
        if (dto.getAvatar() != null) team.setAvatar(dto.getAvatar());
        if (dto.getMaxMembers() != null) team.setMaxMembers(dto.getMaxMembers());
        teamMapper.updateById(team);
        return getTeamDetail(teamId, userId, null);
    }

    @Override
    @Transactional
    public void deleteTeam(Long teamId, Long userId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException(404, "团队不存在");
        }
        if (!team.getCreatorId().equals(userId)) {
            throw new BusinessException(403, "只有创建者可以删除团队");
        }
        teamMapper.deleteById(teamId);
        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamId, teamId);
        teamMemberMapper.delete(wrapper);
    }

    @Override
    public TeamVO getTeamDetail(Long teamId, Long userId, String inviteCode) {
        TeamMember member = teamMemberMapper.selectByTeamAndUser(teamId, userId);
        if (member == null) {
            if (inviteCode == null || inviteCode.isEmpty()) {
                throw new BusinessException(403, "您不是团队成员，请输入邀请码加入团队");
            }
            Team team = teamMapper.selectById(teamId);
            if (team == null) {
                throw new BusinessException(404, "团队不存在");
            }
            if (!team.getInviteCode().equals(inviteCode)) {
                throw new BusinessException(400, "邀请码无效");
            }
            TeamMember newMember = new TeamMember();
            newMember.setTeamId(teamId);
            newMember.setUserId(userId);
            newMember.setRole("MEMBER");
            teamMemberMapper.insert(newMember);
        }
        Team team = teamMapper.selectTeamDetail(teamId);
        if (team == null) {
            throw new BusinessException(404, "团队不存在");
        }
        TeamVO vo = new TeamVO();
        BeanUtils.copyProperties(team, vo);
        List<TeamMemberVO> members = getTeamMembers(teamId);
        vo.setMembers(members);
        return vo;
    }

    @Override
    public PageResult<TeamVO> getTeamPage(int page, int size, String keyword, Long userId) {
        Page<Team> pageParam = new Page<>(page, size);
        List<Team> teams = teamMapper.selectTeamsByUserId(userId);
        List<Long> teamIds = teams.stream().map(Team::getId).collect(Collectors.toList());
        
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        if (teamIds.isEmpty()) {
            wrapper.eq(Team::getId, -1);
        } else {
            wrapper.in(Team::getId, teamIds);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Team::getName, keyword).or().like(Team::getDescription, keyword));
        }
        wrapper.orderByDesc(Team::getCreatedAt);
        Page<Team> result = teamMapper.selectPage(pageParam, wrapper);
        List<TeamVO> records = result.getRecords().stream().map(t -> {
            TeamVO vo = new TeamVO();
            BeanUtils.copyProperties(t, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(result.getTotal(), result.getPages(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public List<TeamVO> getUserTeams(Long userId) {
        List<Team> teams = teamMapper.selectTeamsByUserId(userId);
        return teams.stream().map(t -> {
            TeamVO vo = new TeamVO();
            BeanUtils.copyProperties(t, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void joinTeam(Long userId, String inviteCode) {
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Team::getInviteCode, inviteCode);
        Team team = teamMapper.selectOne(wrapper);
        if (team == null) {
            throw new BusinessException(404, "邀请码无效");
        }
        TeamMember existing = teamMemberMapper.selectByTeamAndUser(team.getId(), userId);
        if (existing != null) {
            throw new BusinessException(400, "已经是团队成员");
        }
        LambdaQueryWrapper<TeamMember> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(TeamMember::getTeamId, team.getId());
        Long count = teamMemberMapper.selectCount(countWrapper);
        if (count >= team.getMaxMembers()) {
            throw new BusinessException(400, "团队已满");
        }
        TeamMember member = new TeamMember();
        member.setTeamId(team.getId());
        member.setUserId(userId);
        member.setRole("MEMBER");
        teamMemberMapper.insert(member);
    }

    @Override
    @Transactional
    public void leaveTeam(Long teamId, Long userId) {
        Team team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException(404, "团队不存在");
        }
        if (team.getCreatorId().equals(userId)) {
            throw new BusinessException(400, "创建者不能退出团队，请先转让所有权");
        }
        teamMemberMapper.deleteByTeamAndUser(teamId, userId);
    }

    @Override
    @Transactional
    public void removeMember(Long teamId, Long operatorId, Long targetUserId) {
        TeamMember operator = teamMemberMapper.selectByTeamAndUser(teamId, operatorId);
        if (operator == null || (!operator.getRole().equals("OWNER") && !operator.getRole().equals("ADMIN"))) {
            throw new BusinessException(403, "无权限移除成员");
        }
        Team team = teamMapper.selectById(teamId);
        if (team != null && team.getCreatorId().equals(targetUserId)) {
            throw new BusinessException(400, "不能移除团队创建者");
        }
        teamMemberMapper.deleteByTeamAndUser(teamId, targetUserId);
    }

    @Override
    @Transactional
    public void updateMemberRole(Long teamId, Long operatorId, Long targetUserId, String role) {
        TeamMember operator = teamMemberMapper.selectByTeamAndUser(teamId, operatorId);
        if (operator == null || !operator.getRole().equals("OWNER")) {
            throw new BusinessException(403, "只有创建者可以更改角色");
        }
        TeamMember target = teamMemberMapper.selectByTeamAndUser(teamId, targetUserId);
        if (target == null) {
            throw new BusinessException(404, "成员不存在");
        }
        target.setRole(role);
        teamMemberMapper.updateById(target);
    }

    @Override
    public List<TeamMemberVO> getTeamMembers(Long teamId) {
        List<TeamMember> members = teamMemberMapper.selectMembersByTeamId(teamId);
        return members.stream().map(m -> {
            TeamMemberVO vo = new TeamMemberVO();
            BeanUtils.copyProperties(m, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}
