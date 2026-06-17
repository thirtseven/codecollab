package com.codecollab.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.codecollab.common.Constants;
import com.codecollab.common.PageResult;
import com.codecollab.entity.*;
import com.codecollab.exception.BusinessException;
import com.codecollab.mapper.*;
import com.codecollab.service.AdminService;
import com.codecollab.vo.UserVO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<UserMapper, User> implements AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TeamMapper teamMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private CodeSubmissionMapper submissionMapper;

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public PageResult<UserVO> getUserPage(int page, int size, String keyword, String role) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                .or().like(User::getEmail, keyword)
                .or().like(User::getNickname, keyword));
        }
        if (role != null && !role.isEmpty()) {
            wrapper.eq(User::getRole, role);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        Page<User> result = userMapper.selectPage(pageParam, wrapper);
        List<UserVO> records = result.getRecords().stream().map(u -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(u, vo);
            return vo;
        }).collect(Collectors.toList());
        return PageResult.of(result.getTotal(), result.getPages(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public void updateUserRole(Long operatorId, Long targetUserId, String role) {
        User operator = userMapper.selectById(operatorId);
        if (operator == null || !operator.getRole().equals(Constants.ROLE_ADMIN)) {
            throw new BusinessException(403, "只有管理员可以更改用户角色");
        }
        User target = userMapper.selectById(targetUserId);
        if (target == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (target.getRole().equals(Constants.ROLE_ADMIN) && !role.equals(Constants.ROLE_ADMIN)) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getRole, Constants.ROLE_ADMIN);
            Long adminCount = userMapper.selectCount(wrapper);
            if (adminCount <= 1) {
                throw new BusinessException(400, "不能移除最后一个管理员的角色");
            }
        }
        target.setRole(role);
        userMapper.updateById(target);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (user.getRole().equals(Constants.ROLE_ADMIN)) {
            throw new BusinessException(400, "不能删除管理员账号");
        }
        userMapper.deleteById(userId);
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userMapper.selectCount(null));
        stats.put("totalTeams", teamMapper.selectCount(null));
        stats.put("totalProjects", projectMapper.selectCount(null));
        stats.put("totalSubmissions", submissionMapper.selectCount(null));
        LambdaQueryWrapper<User> activeWrapper = new LambdaQueryWrapper<>();
        activeWrapper.eq(User::getStatus, Constants.STATUS_ACTIVE);
        stats.put("activeUsers", userMapper.selectCount(activeWrapper));
        stats.put("pendingSubmissions", submissionMapper.selectCount(
            new LambdaQueryWrapper<CodeSubmission>().eq(CodeSubmission::getStatus, "PENDING")));
        return stats;
    }

    @Override
    public List<Map<String, Object>> getSystemConfigs() {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        List<SystemConfig> configs = systemConfigMapper.selectList(wrapper);
        return configs.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("key", c.getConfigKey());
            map.put("value", c.getConfigValue());
            map.put("description", c.getDescription());
            map.put("type", c.getType());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public void updateSystemConfig(String key, String value) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, key);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);
        if (config != null) {
            config.setConfigValue(value);
            systemConfigMapper.updateById(config);
        } else {
            throw new BusinessException(404, "配置不存在");
        }
    }

    @Override
    public byte[] exportUsers(String format) {
        List<User> users = userMapper.selectList(null);
        if ("csv".equalsIgnoreCase(format)) {
            return exportUsersToCsv(users);
        }
        return exportUsersToExcel(users);
    }

    @Override
    public byte[] exportSubmissions(Long projectId, String format) {
        LambdaQueryWrapper<CodeSubmission> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(CodeSubmission::getProjectId, projectId);
        }
        List<CodeSubmission> submissions = submissionMapper.selectList(wrapper);
        if ("csv".equalsIgnoreCase(format)) {
            return exportSubmissionsToCsv(submissions);
        }
        return exportSubmissionsToExcel(submissions);
    }

    private byte[] exportUsersToExcel(List<User> users) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Users");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Username");
            header.createCell(2).setCellValue("Email");
            header.createCell(3).setCellValue("Role");
            header.createCell(4).setCellValue("Status");
            header.createCell(5).setCellValue("Created At");
            int rowIdx = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getUsername());
                row.createCell(2).setCellValue(user.getEmail());
                row.createCell(3).setCellValue(user.getRole());
                row.createCell(4).setCellValue(user.getStatus());
                row.createCell(5).setCellValue(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "");
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException(500, "Export failed");
        }
    }

    private byte[] exportUsersToCsv(List<User> users) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Username,Email,Role,Status,Created At\n");
        for (User user : users) {
            sb.append(user.getId()).append(",")
                .append(user.getUsername()).append(",")
                .append(user.getEmail()).append(",")
                .append(user.getRole()).append(",")
                .append(user.getStatus()).append(",")
                .append(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "").append("\n");
        }
        return sb.toString().getBytes();
    }

    private byte[] exportSubmissionsToExcel(List<CodeSubmission> submissions) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Submissions");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Project ID");
            header.createCell(2).setCellValue("User ID");
            header.createCell(3).setCellValue("Title");
            header.createCell(4).setCellValue("Status");
            header.createCell(5).setCellValue("Created At");
            int rowIdx = 1;
            for (CodeSubmission sub : submissions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(sub.getId());
                row.createCell(1).setCellValue(sub.getProjectId());
                row.createCell(2).setCellValue(sub.getUserId());
                row.createCell(3).setCellValue(sub.getTitle());
                row.createCell(4).setCellValue(sub.getStatus());
                row.createCell(5).setCellValue(sub.getCreatedAt() != null ? sub.getCreatedAt().toString() : "");
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new BusinessException(500, "Export failed");
        }
    }

    private byte[] exportSubmissionsToCsv(List<CodeSubmission> submissions) {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Project ID,User ID,Title,Status,Created At\n");
        for (CodeSubmission sub : submissions) {
            sb.append(sub.getId()).append(",")
                .append(sub.getProjectId()).append(",")
                .append(sub.getUserId()).append(",")
                .append(sub.getTitle()).append(",")
                .append(sub.getStatus()).append(",")
                .append(sub.getCreatedAt() != null ? sub.getCreatedAt().toString() : "").append("\n");
        }
        return sb.toString().getBytes();
    }
}
