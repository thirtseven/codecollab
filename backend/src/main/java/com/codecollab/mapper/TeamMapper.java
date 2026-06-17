package com.codecollab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.codecollab.entity.Team;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface TeamMapper extends BaseMapper<Team> {
    @Select("SELECT t.*, u.username as creator_name, (SELECT COUNT(*) FROM team_members WHERE team_id = t.id) as member_count FROM teams t LEFT JOIN users u ON t.creator_id = u.id WHERE t.deleted = 0 ORDER BY t.created_at DESC")
    IPage<Team> selectTeamPage(Page<Team> page);

    @Select("SELECT t.*, u.username as creator_name, (SELECT COUNT(*) FROM team_members WHERE team_id = t.id) as member_count FROM teams t LEFT JOIN users u ON t.creator_id = u.id WHERE t.id = #{id} AND t.deleted = 0")
    Team selectTeamDetail(@Param("id") Long id);

    @Select("SELECT t.*, u.username as creator_name, (SELECT COUNT(*) FROM team_members WHERE team_id = t.id) as member_count FROM teams t LEFT JOIN users u ON t.creator_id = u.id INNER JOIN team_members tm ON t.id = tm.team_id WHERE tm.user_id = #{userId} AND t.deleted = 0")
    List<Team> selectTeamsByUserId(@Param("userId") Long userId);
}
