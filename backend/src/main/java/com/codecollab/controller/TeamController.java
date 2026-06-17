package com.codecollab.controller;

import com.codecollab.common.PageResult;
import com.codecollab.common.Result;
import com.codecollab.dto.TeamDTO;
import com.codecollab.service.TeamService;
import com.codecollab.vo.TeamMemberVO;
import com.codecollab.vo.TeamVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    @PostMapping
    public Result<TeamVO> createTeam(HttpServletRequest request, @Valid @RequestBody TeamDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(teamService.createTeam(userId, dto));
    }

    @PutMapping("/{id}")
    public Result<TeamVO> updateTeam(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody TeamDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(teamService.updateTeam(id, userId, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteTeam(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        teamService.deleteTeam(id, userId);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<TeamVO> getTeamDetail(@PathVariable Long id) {
        return Result.success(teamService.getTeamDetail(id));
    }

    @GetMapping
    public Result<PageResult<TeamVO>> getTeamPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(teamService.getTeamPage(page, size, keyword));
    }

    @GetMapping("/my")
    public Result<List<TeamVO>> getMyTeams(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(teamService.getUserTeams(userId));
    }

    @PostMapping("/join")
    public Result<Void> joinTeam(HttpServletRequest request, @RequestParam String inviteCode) {
        Long userId = (Long) request.getAttribute("userId");
        teamService.joinTeam(userId, inviteCode);
        return Result.success();
    }

    @PostMapping("/{id}/leave")
    public Result<Void> leaveTeam(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        teamService.leaveTeam(id, userId);
        return Result.success();
    }

    @DeleteMapping("/{teamId}/members/{userId}")
    public Result<Void> removeMember(HttpServletRequest request, @PathVariable Long teamId, @PathVariable Long userId) {
        Long operatorId = (Long) request.getAttribute("userId");
        teamService.removeMember(teamId, operatorId, userId);
        return Result.success();
    }

    @PutMapping("/{teamId}/members/{userId}/role")
    public Result<Void> updateMemberRole(HttpServletRequest request, @PathVariable Long teamId,
                                          @PathVariable Long userId, @RequestParam String role) {
        Long operatorId = (Long) request.getAttribute("userId");
        teamService.updateMemberRole(teamId, operatorId, userId, role);
        return Result.success();
    }

    @GetMapping("/{id}/members")
    public Result<List<TeamMemberVO>> getTeamMembers(@PathVariable Long id) {
        return Result.success(teamService.getTeamMembers(id));
    }
}
