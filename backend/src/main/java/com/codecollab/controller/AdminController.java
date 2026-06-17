package com.codecollab.controller;

import com.codecollab.common.Constants;
import com.codecollab.common.PageResult;
import com.codecollab.common.Result;
import com.codecollab.exception.BusinessException;
import com.codecollab.service.AdminService;
import com.codecollab.vo.AdminUserVO;
import com.codecollab.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    private void checkAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!Constants.ROLE_ADMIN.equals(role)) {
            throw new BusinessException(403, "需要管理员权限");
        }
    }

    @GetMapping("/users")
    public Result<PageResult<UserVO>> getUsers(HttpServletRequest request,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String role) {
        checkAdmin(request);
        return Result.success(adminService.getUserPage(page, size, keyword, role));
    }

    @GetMapping("/users/detail")
    public Result<PageResult<AdminUserVO>> getAdminUsers(HttpServletRequest request,
                                                          @RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "10") int size,
                                                          @RequestParam(required = false) String keyword,
                                                          @RequestParam(required = false) String role) {
        checkAdmin(request);
        return Result.success(adminService.getAdminUserPage(page, size, keyword, role));
    }

    @GetMapping("/users/{id}")
    public Result<AdminUserVO> getAdminUserDetail(HttpServletRequest request, @PathVariable Long id) {
        checkAdmin(request);
        return Result.success(adminService.getAdminUserDetail(id));
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(HttpServletRequest request, @PathVariable Long id, @RequestParam Integer status) {
        checkAdmin(request);
        adminService.updateUserStatus(id, status);
        return Result.success();
    }

    @PutMapping("/users/{id}/role")
    public Result<Void> updateUserRole(HttpServletRequest request, @PathVariable Long id, @RequestParam String role) {
        checkAdmin(request);
        Long operatorId = (Long) request.getAttribute("userId");
        adminService.updateUserRole(operatorId, id, role);
        return Result.success();
    }

    @PutMapping("/users/{id}/password")
    public Result<Void> resetPassword(HttpServletRequest request, @PathVariable Long id, @RequestParam String newPassword) {
        checkAdmin(request);
        adminService.resetUserPassword(id, newPassword);
        return Result.success();
    }

    @PutMapping("/users/{id}/update")
    public Result<Void> updateUser(HttpServletRequest request, @PathVariable Long id,
                                    @RequestParam(required = false) String username,
                                    @RequestParam(required = false) String email,
                                    @RequestParam(required = false) String nickname,
                                    @RequestParam(required = false) String phone,
                                    @RequestParam(required = false) String role) {
        checkAdmin(request);
        adminService.updateUser(id, username, email, nickname, phone, role);
        return Result.success();
    }

    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(HttpServletRequest request, @PathVariable Long id) {
        checkAdmin(request);
        adminService.deleteUser(id);
        return Result.success();
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats(HttpServletRequest request) {
        checkAdmin(request);
        return Result.success(adminService.getDashboardStats());
    }

    @GetMapping("/configs")
    public Result<List<Map<String, Object>>> getSystemConfigs(HttpServletRequest request) {
        checkAdmin(request);
        return Result.success(adminService.getSystemConfigs());
    }

    @PutMapping("/configs/{key}")
    public Result<Void> updateSystemConfig(HttpServletRequest request, @PathVariable String key, @RequestParam String value) {
        checkAdmin(request);
        adminService.updateSystemConfig(key, value);
        return Result.success();
    }

    @GetMapping("/export/users")
    public byte[] exportUsers(HttpServletRequest request, @RequestParam(defaultValue = "excel") String format) {
        checkAdmin(request);
        return adminService.exportUsers(format);
    }

    @GetMapping("/export/submissions")
    public byte[] exportSubmissions(HttpServletRequest request,
                                     @RequestParam(required = false) Long projectId,
                                     @RequestParam(defaultValue = "excel") String format) {
        checkAdmin(request);
        return adminService.exportSubmissions(projectId, format);
    }
}
