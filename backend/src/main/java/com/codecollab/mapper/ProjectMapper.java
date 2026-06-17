package com.codecollab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.codecollab.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
    @Select("SELECT p.*, t.name as team_name, u.username as creator_name, (SELECT COUNT(*) FROM project_files WHERE project_id = p.id AND is_directory = 0) as file_count, (SELECT COUNT(*) FROM code_submissions WHERE project_id = p.id) as submission_count FROM projects p LEFT JOIN teams t ON p.team_id = t.id LEFT JOIN users u ON p.creator_id = u.id WHERE p.deleted = 0 AND (p.team_id = #{teamId} OR #{teamId} IS NULL) ORDER BY p.created_at DESC")
    IPage<Project> selectProjectPage(Page<Project> page, @Param("teamId") Long teamId);

    @Select("SELECT p.*, t.name as team_name, u.username as creator_name FROM projects p LEFT JOIN teams t ON p.team_id = t.id LEFT JOIN users u ON p.creator_id = u.id WHERE p.id = #{id} AND p.deleted = 0")
    Project selectProjectDetail(@Param("id") Long id);
}
