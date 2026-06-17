package com.codecollab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.codecollab.entity.ProjectFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface ProjectFileMapper extends BaseMapper<ProjectFile> {
    @Select("SELECT * FROM project_files WHERE project_id = #{projectId} AND deleted = 0 ORDER BY is_directory DESC, file_path ASC")
    List<ProjectFile> selectByProjectId(@Param("projectId") Long projectId);

    @Select("SELECT * FROM project_files WHERE project_id = #{projectId} AND file_path = #{filePath} AND deleted = 0")
    ProjectFile selectByProjectAndPath(@Param("projectId") Long projectId, @Param("filePath") String filePath);
}
