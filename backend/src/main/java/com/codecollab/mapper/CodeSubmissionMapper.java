package com.codecollab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.codecollab.entity.CodeSubmission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CodeSubmissionMapper extends BaseMapper<CodeSubmission> {
    @Select("SELECT cs.*, u.username, p.name as project_name, (SELECT COUNT(*) FROM submission_files WHERE submission_id = cs.id) as file_count FROM code_submissions cs LEFT JOIN users u ON cs.user_id = u.id LEFT JOIN projects p ON cs.project_id = p.id WHERE cs.deleted = 0 AND (cs.project_id = #{projectId} OR #{projectId} IS NULL) ORDER BY cs.created_at DESC")
    IPage<CodeSubmission> selectSubmissionPage(Page<CodeSubmission> page, @Param("projectId") Long projectId);

    @Select("SELECT cs.*, u.username, p.name as project_name FROM code_submissions cs LEFT JOIN users u ON cs.user_id = u.id LEFT JOIN projects p ON cs.project_id = p.id WHERE cs.id = #{id} AND cs.deleted = 0")
    CodeSubmission selectSubmissionDetail(@Param("id") Long id);
}
