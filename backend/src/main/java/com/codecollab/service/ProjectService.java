package com.codecollab.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.codecollab.common.PageResult;
import com.codecollab.dto.FileDTO;
import com.codecollab.dto.ProjectDTO;
import com.codecollab.entity.Project;
import com.codecollab.entity.ProjectFile;
import com.codecollab.vo.ProjectVO;
import java.util.List;

public interface ProjectService extends IService<Project> {
    ProjectVO createProject(Long userId, ProjectDTO dto);
    ProjectVO updateProject(Long projectId, Long userId, ProjectDTO dto);
    void deleteProject(Long projectId, Long userId);
    ProjectVO getProjectDetail(Long projectId);
    PageResult<ProjectVO> getProjectPage(int page, int size, Long teamId, String keyword, Long userId);
    List<ProjectFile> getProjectFiles(Long projectId);
    ProjectFile getFileContent(Long fileId);
    ProjectFile saveFile(Long projectId, Long userId, FileDTO dto);
    void deleteFile(Long fileId, Long userId);
    void createDirectory(Long projectId, Long userId, String dirPath);
}
