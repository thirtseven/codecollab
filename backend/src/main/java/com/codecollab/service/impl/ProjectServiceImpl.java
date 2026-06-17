package com.codecollab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.codecollab.common.Constants;
import com.codecollab.common.PageResult;
import com.codecollab.dto.FileDTO;
import com.codecollab.dto.ProjectDTO;
import com.codecollab.entity.CodeSubmission;
import com.codecollab.entity.Project;
import com.codecollab.entity.ProjectFile;
import com.codecollab.entity.SubmissionFile;
import com.codecollab.entity.TeamMember;
import com.codecollab.exception.BusinessException;
import com.codecollab.mapper.CodeSubmissionMapper;
import com.codecollab.mapper.ProjectFileMapper;
import com.codecollab.mapper.ProjectMapper;
import com.codecollab.mapper.SubmissionFileMapper;
import com.codecollab.mapper.TeamMemberMapper;
import com.codecollab.service.ProjectService;
import com.codecollab.vo.ProjectVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<ProjectMapper, Project> implements ProjectService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectFileMapper projectFileMapper;

    @Autowired
    private TeamMemberMapper teamMemberMapper;

    @Autowired
    private CodeSubmissionMapper submissionMapper;

    @Autowired
    private SubmissionFileMapper submissionFileMapper;

    @Override
    @Transactional
    public ProjectVO createProject(Long userId, ProjectDTO dto) {
        if (dto.getTeamId() == null) {
            throw new BusinessException(400, "请选择团队");
        }
        TeamMember member = teamMemberMapper.selectByTeamAndUser(dto.getTeamId(), userId);
        if (member == null) {
            throw new BusinessException(403, "您不是该团队成员");
        }
        Project project = new Project();
        BeanUtils.copyProperties(dto, project);
        project.setCreatorId(userId);
        project.setStatus(Constants.STATUS_ACTIVE);
        projectMapper.insert(project);

        ProjectFile rootDir = new ProjectFile();
        rootDir.setProjectId(project.getId());
        rootDir.setFilePath("/");
        rootDir.setFileName("root");
        rootDir.setIsDirectory(1);
        rootDir.setCreatorId(userId);
        rootDir.setVersion(1);
        projectFileMapper.insert(rootDir);

        return getProjectDetail(project.getId());
    }

    @Override
    @Transactional
    public ProjectVO updateProject(Long projectId, Long userId, ProjectDTO dto) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(404, "项目不存在");
        }
        if (!project.getCreatorId().equals(userId)) {
            TeamMember member = teamMemberMapper.selectByTeamAndUser(project.getTeamId(), userId);
            if (member == null || (!member.getRole().equals("OWNER") && !member.getRole().equals("ADMIN"))) {
                throw new BusinessException(403, "无权限更新项目");
            }
        }
        if (dto.getName() != null) project.setName(dto.getName());
        if (dto.getDescription() != null) project.setDescription(dto.getDescription());
        if (dto.getLanguage() != null) project.setLanguage(dto.getLanguage());
        if (dto.getIsPublic() != null) project.setIsPublic(dto.getIsPublic());
        projectMapper.updateById(project);
        return getProjectDetail(projectId);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId, Long userId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(404, "项目不存在");
        }
        boolean isCreator = project.getCreatorId().equals(userId);
        if (!isCreator) {
            TeamMember member = teamMemberMapper.selectByTeamAndUser(project.getTeamId(), userId);
            if (member == null || (!member.getRole().equals("OWNER") && !member.getRole().equals("ADMIN"))) {
                throw new BusinessException(403, "只有创建者或团队管理员可以删除项目");
            }
        }
        projectMapper.deleteById(projectId);
        LambdaQueryWrapper<ProjectFile> fileWrapper = new LambdaQueryWrapper<>();
        fileWrapper.eq(ProjectFile::getProjectId, projectId);
        projectFileMapper.delete(fileWrapper);

        LambdaQueryWrapper<CodeSubmission> subWrapper = new LambdaQueryWrapper<>();
        subWrapper.eq(CodeSubmission::getProjectId, projectId);
        List<CodeSubmission> submissions = submissionMapper.selectList(subWrapper);
        for (CodeSubmission sub : submissions) {
            LambdaQueryWrapper<SubmissionFile> sfWrapper = new LambdaQueryWrapper<>();
            sfWrapper.eq(SubmissionFile::getSubmissionId, sub.getId());
            submissionFileMapper.delete(sfWrapper);
        }
        submissionMapper.delete(subWrapper);
    }

    @Override
    public ProjectVO getProjectDetail(Long projectId) {
        Project project = projectMapper.selectProjectDetail(projectId);
        if (project == null) {
            throw new BusinessException(404, "项目不存在");
        }
        ProjectVO vo = new ProjectVO();
        BeanUtils.copyProperties(project, vo);
        return vo;
    }

    @Override
    public PageResult<ProjectVO> getProjectPage(int page, int size, Long teamId, String keyword) {
        Page<Project> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        if (teamId != null) {
            wrapper.eq(Project::getTeamId, teamId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Project::getName, keyword).or().like(Project::getDescription, keyword));
        }
        wrapper.orderByDesc(Project::getCreatedAt);
        Page<Project> result = projectMapper.selectPage(pageParam, wrapper);
        List<ProjectVO> records = result.getRecords().stream().map(p -> {
            ProjectVO vo = new ProjectVO();
            BeanUtils.copyProperties(p, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(result.getTotal(), result.getPages(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public List<ProjectFile> getProjectFiles(Long projectId) {
        return projectFileMapper.selectByProjectId(projectId);
    }

    @Override
    public ProjectFile getFileContent(Long fileId) {
        ProjectFile file = projectFileMapper.selectById(fileId);
        if (file == null) {
            throw new BusinessException(404, "文件不存在");
        }
        return file;
    }

    @Override
    @Transactional
    public ProjectFile saveFile(Long projectId, Long userId, FileDTO dto) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(404, "项目不存在");
        }
        ProjectFile existing = projectFileMapper.selectByProjectAndPath(projectId, dto.getFilePath());
        if (existing != null) {
            existing.setContent(dto.getContent());
            existing.setVersion(existing.getVersion() + 1);
            existing.setFileSize(dto.getContent() != null ? (long) dto.getContent().length() : 0);
            projectFileMapper.updateById(existing);
            return existing;
        } else {
            ProjectFile file = new ProjectFile();
            file.setProjectId(projectId);
            file.setFilePath(dto.getFilePath());
            file.setFileName(dto.getFileName());
            file.setContent(dto.getContent());
            file.setFileType(dto.getFileType());
            file.setFileSize(dto.getContent() != null ? (long) dto.getContent().length() : 0);
            file.setCreatorId(userId);
            file.setParentId(dto.getParentId());
            file.setIsDirectory(0);
            file.setVersion(1);
            projectFileMapper.insert(file);
            return file;
        }
    }

    @Override
    @Transactional
    public void deleteFile(Long fileId, Long userId) {
        ProjectFile file = projectFileMapper.selectById(fileId);
        if (file == null) {
            throw new BusinessException(404, "文件不存在");
        }
        projectFileMapper.deleteById(fileId);
    }

    @Override
    @Transactional
    public void createDirectory(Long projectId, Long userId, String dirPath) {
        ProjectFile existing = projectFileMapper.selectByProjectAndPath(projectId, dirPath);
        if (existing != null) {
            throw new BusinessException(400, "目录已存在");
        }
        String[] parts = dirPath.split("/");
        String dirName = parts[parts.length - 1];
        ProjectFile dir = new ProjectFile();
        dir.setProjectId(projectId);
        dir.setFilePath(dirPath);
        dir.setFileName(dirName);
        dir.setIsDirectory(1);
        dir.setCreatorId(userId);
        dir.setVersion(1);
        projectFileMapper.insert(dir);
    }
}
