package com.codecollab.controller;

import com.codecollab.common.PageResult;
import com.codecollab.common.Result;
import com.codecollab.dto.FileDTO;
import com.codecollab.dto.ProjectDTO;
import com.codecollab.entity.ProjectFile;
import com.codecollab.service.ProjectService;
import com.codecollab.vo.ProjectVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping
    public Result<ProjectVO> createProject(HttpServletRequest request, @Valid @RequestBody ProjectDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(projectService.createProject(userId, dto));
    }

    @PutMapping("/{id}")
    public Result<ProjectVO> updateProject(HttpServletRequest request, @PathVariable Long id, @Valid @RequestBody ProjectDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(projectService.updateProject(id, userId, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteProject(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        projectService.deleteProject(id, userId);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<ProjectVO> getProjectDetail(@PathVariable Long id) {
        return Result.success(projectService.getProjectDetail(id));
    }

    @GetMapping
    public Result<PageResult<ProjectVO>> getProjectPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String keyword) {
        return Result.success(projectService.getProjectPage(page, size, teamId, keyword));
    }

    @GetMapping("/{id}/files")
    public Result<List<ProjectFile>> getProjectFiles(@PathVariable Long id) {
        return Result.success(projectService.getProjectFiles(id));
    }

    @GetMapping("/files/{fileId}")
    public Result<ProjectFile> getFileContent(@PathVariable Long fileId) {
        return Result.success(projectService.getFileContent(fileId));
    }

    @PostMapping("/{id}/files")
    public Result<ProjectFile> saveFile(HttpServletRequest request, @PathVariable Long id, @RequestBody FileDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(projectService.saveFile(id, userId, dto));
    }

    @DeleteMapping("/files/{fileId}")
    public Result<Void> deleteFile(HttpServletRequest request, @PathVariable Long fileId) {
        Long userId = (Long) request.getAttribute("userId");
        projectService.deleteFile(fileId, userId);
        return Result.success();
    }

    @PostMapping("/{id}/directories")
    public Result<Void> createDirectory(HttpServletRequest request, @PathVariable Long id, @RequestParam String dirPath) {
        Long userId = (Long) request.getAttribute("userId");
        projectService.createDirectory(id, userId, dirPath);
        return Result.success();
    }
}
