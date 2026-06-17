package com.codecollab.controller;

import com.codecollab.common.PageResult;
import com.codecollab.common.Result;
import com.codecollab.dto.SubmissionDTO;
import com.codecollab.entity.SubmissionFile;
import com.codecollab.service.CodeSubmissionService;
import com.codecollab.vo.SubmissionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/submissions")
public class CodeSubmissionController {

    @Autowired
    private CodeSubmissionService submissionService;

    @PostMapping
    public Result<SubmissionVO> createSubmission(HttpServletRequest request, @Valid @RequestBody SubmissionDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(submissionService.createSubmission(userId, dto));
    }

    @GetMapping("/{id}")
    public Result<SubmissionVO> getSubmissionDetail(@PathVariable Long id) {
        return Result.success(submissionService.getSubmissionDetail(id));
    }

    @GetMapping
    public Result<PageResult<SubmissionVO>> getSubmissionPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String status) {
        return Result.success(submissionService.getSubmissionPage(page, size, projectId, status));
    }

    @PostMapping("/{id}/review")
    public Result<Void> reviewSubmission(HttpServletRequest request, @PathVariable Long id,
                                          @RequestParam String status, @RequestParam(required = false) String comment) {
        Long reviewerId = (Long) request.getAttribute("userId");
        submissionService.reviewSubmission(id, reviewerId, status, comment);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteSubmission(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        submissionService.deleteSubmission(id, userId);
        return Result.success();
    }

    @GetMapping("/{id}/files")
    public Result<List<SubmissionFile>> getSubmissionFiles(@PathVariable Long id) {
        return Result.success(submissionService.getSubmissionFiles(id));
    }

    @GetMapping("/project/{projectId}")
    public Result<List<SubmissionVO>> getProjectSubmissions(@PathVariable Long projectId,
                                                            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(submissionService.getProjectSubmissions(projectId, limit));
    }
}
