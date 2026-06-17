package com.codecollab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.codecollab.entity.SubmissionFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface SubmissionFileMapper extends BaseMapper<SubmissionFile> {
    @Select("SELECT * FROM submission_files WHERE submission_id = #{submissionId}")
    List<SubmissionFile> selectBySubmissionId(@Param("submissionId") Long submissionId);
}
