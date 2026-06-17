package com.codecollab.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.codecollab.common.PageResult;
import com.codecollab.dto.SubmissionDTO;
import com.codecollab.entity.CodeSubmission;
import com.codecollab.entity.SubmissionFile;
import com.codecollab.vo.SubmissionVO;
import java.util.List;

public interface CodeSubmissionService extends IService<CodeSubmission> {
    SubmissionVO createSubmission(Long userId, SubmissionDTO dto);
    SubmissionVO getSubmissionDetail(Long submissionId);
    PageResult<SubmissionVO> getSubmissionPage(int page, int size, Long projectId, String status);
    void reviewSubmission(Long submissionId, Long reviewerId, String status, String comment);
    void deleteSubmission(Long submissionId, Long userId);
    List<SubmissionFile> getSubmissionFiles(Long submissionId);
    List<SubmissionVO> getProjectSubmissions(Long projectId, int limit);
}
