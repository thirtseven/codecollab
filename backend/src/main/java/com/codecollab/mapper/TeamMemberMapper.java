package com.codecollab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.codecollab.entity.TeamMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;
import java.util.List;

@Mapper
public interface TeamMemberMapper extends BaseMapper<TeamMember> {
    @Select("SELECT tm.*, u.username, u.avatar, u.email FROM team_members tm LEFT JOIN users u ON tm.user_id = u.id WHERE tm.team_id = #{teamId}")
    List<TeamMember> selectMembersByTeamId(@Param("teamId") Long teamId);

    @Select("SELECT * FROM team_members WHERE team_id = #{teamId} AND user_id = #{userId}")
    TeamMember selectByTeamAndUser(@Param("teamId") Long teamId, @Param("userId") Long userId);

    @Delete("DELETE FROM team_members WHERE team_id = #{teamId} AND user_id = #{userId}")
    int deleteByTeamAndUser(@Param("teamId") Long teamId, @Param("userId") Long userId);
}
