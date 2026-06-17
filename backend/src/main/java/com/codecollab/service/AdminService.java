package com.codecollab.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.codecollab.entity.User;
import com.codecollab.vo.AdminUserVO;
import com.codecollab.vo.UserVO;
import com.codecollab.common.PageResult;
import java.util.List;
import java.util.Map;

public interface AdminService extends IService<User> {
    PageResult<UserVO> getUserPage(int page, int size, String keyword, String role);
    PageResult<AdminUserVO> getAdminUserPage(int page, int size, String keyword, String role);
    AdminUserVO getAdminUserDetail(Long userId);
    void updateUserStatus(Long userId, Integer status);
    void updateUserRole(Long operatorId, Long targetUserId, String role);
    void resetUserPassword(Long userId, String newPassword);
    void updateUser(Long userId, String username, String email, String nickname, String phone, String role);
    void deleteUser(Long userId);
    Map<String, Object> getDashboardStats();
    List<Map<String, Object>> getSystemConfigs();
    void updateSystemConfig(String key, String value);
    byte[] exportUsers(String format);
    byte[] exportSubmissions(Long projectId, String format);
}
