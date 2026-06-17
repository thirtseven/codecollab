package com.codecollab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.codecollab.common.PageResult;
import com.codecollab.dto.SubmissionDTO;
import com.codecollab.entity.CodeSubmission;
import com.codecollab.entity.SubmissionFile;
import com.codecollab.exception.BusinessException;
import com.codecollab.mapper.CodeSubmissionMapper;
import com.codecollab.mapper.SubmissionFileMapper;
import com.codecollab.service.CodeSubmissionService;
import com.codecollab.vo.SubmissionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CodeSubmissionServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<CodeSubmissionMapper, CodeSubmission> implements CodeSubmissionService {

    @Autowired
    private CodeSubmissionMapper submissionMapper;

    @Autowired
    private SubmissionFileMapper submissionFileMapper;

    @Override
    @Transactional
    public SubmissionVO createSubmission(Long userId, SubmissionDTO dto) {
        CodeSubmission submission = new CodeSubmission();
        submission.setProjectId(dto.getProjectId());
        submission.setUserId(userId);
        submission.setTitle(dto.getTitle());
        submission.setDescription(dto.getDescription());
        submission.setBranch(dto.getBranch() != null ? dto.getBranch() : "main");
        submission.setStatus("PENDING");
        submissionMapper.insert(submission);

        if (dto.getFiles() != null) {
            for (SubmissionDTO.SubmissionFileDTO fileDTO : dto.getFiles()) {
                SubmissionFile file = new SubmissionFile();
                file.setSubmissionId(submission.getId());
                file.setFilePath(fileDTO.getFilePath());
                file.setFileName(fileDTO.getFileName());
                file.setContent(fileDTO.getContent());
                file.setDiffContent(fileDTO.getDiffContent());
                file.setChangeType(fileDTO.getChangeType() != null ? fileDTO.getChangeType() : "MODIFY");
                file.setFileSize(fileDTO.getContent() != null ? (long) fileDTO.getContent().length() : 0);
                submissionFileMapper.insert(file);
            }
        }
        return getSubmissionDetail(submission.getId());
    }

    @Override
    public SubmissionVO getSubmissionDetail(Long submissionId) {
        CodeSubmission submission = submissionMapper.selectSubmissionDetail(submissionId);
        if (submission == null) {
            throw new BusinessException(404, "提交不存在");
        }
        SubmissionVO vo = new SubmissionVO();
        BeanUtils.copyProperties(submission, vo);
        LambdaQueryWrapper<SubmissionFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubmissionFile::getSubmissionId, submissionId);
        vo.setFileCount(Math.toIntExact(submissionFileMapper.selectCount(wrapper)));
        return vo;
    }

    @Override
    public PageResult<SubmissionVO> getSubmissionPage(int page, int size, Long projectId, String status) {
        Page<CodeSubmission> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CodeSubmission> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(CodeSubmission::getProjectId, projectId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(CodeSubmission::getStatus, status);
        }
        wrapper.orderByDesc(CodeSubmission::getCreatedAt);
        Page<CodeSubmission> result = submissionMapper.selectPage(pageParam, wrapper);
        List<SubmissionVO> records = result.getRecords().stream().map(s -> {
            SubmissionVO vo = new SubmissionVO();
            BeanUtils.copyProperties(s, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(result.getTotal(), result.getPages(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    @Transactional
    public void reviewSubmission(Long submissionId, Long reviewerId, String status, String comment) {
        CodeSubmission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(404, "提交不存在");
        }
        if (!submission.getStatus().equals("PENDING")) {
            throw new BusinessException(400, "提交已审核，不能重复审核");
        }
        if (!status.equals("APPROVED") && !status.equals("REJECTED")) {
            throw new BusinessException(400, "审核状态无效，只能是 APPROVED 或 REJECTED");
        }
        if (status.equals("REJECTED") && (comment == null || comment.trim().isEmpty())) {
            throw new BusinessException(400, "驳回时必须填写审核意见");
        }
        submission.setStatus(status);
        submission.setReviewComment(comment);
        submission.setReviewerId(reviewerId);
        submission.setReviewedAt(java.time.LocalDateTime.now());
        submissionMapper.updateById(submission);
    }

    @Override
    @Transactional
    public void deleteSubmission(Long submissionId, Long userId) {
        CodeSubmission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException(404, "提交不存在");
        }
        if (!submission.getUserId().equals(userId)) {
            throw new BusinessException(403, "只有创建者可以删除提交");
        }
        submissionMapper.deleteById(submissionId);
        LambdaQueryWrapper<SubmissionFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SubmissionFile::getSubmissionId, submissionId);
        submissionFileMapper.delete(wrapper);
    }

    @Override
    public List<SubmissionFile> getSubmissionFiles(Long submissionId) {
        return submissionFileMapper.selectBySubmissionId(submissionId);
    }

    @Override
    public List<SubmissionVO> getProjectSubmissions(Long projectId, int limit) {
        Page<CodeSubmission> pageParam = new Page<>(1, limit);
        LambdaQueryWrapper<CodeSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CodeSubmission::getProjectId, projectId);
        wrapper.orderByDesc(CodeSubmission::getCreatedAt);
        Page<CodeSubmission> result = submissionMapper.selectPage(pageParam, wrapper);
        return result.getRecords().stream().map(s -> {
            SubmissionVO vo = new SubmissionVO();
            BeanUtils.copyProperties(s, vo);
            return vo;
        }).collect(Collectors.toList());
    }
}
