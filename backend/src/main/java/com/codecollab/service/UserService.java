package com.codecollab.service;

import com.codecollab.dto.LoginDTO;
import com.codecollab.dto.RegisterDTO;
import com.codecollab.dto.UpdateProfileDTO;
import com.codecollab.vo.LoginVO;
import com.codecollab.vo.UserVO;

public interface UserService {
    LoginVO login(LoginDTO dto, String ip);
    UserVO register(RegisterDTO dto);
    UserVO adminRegister(RegisterDTO dto);
    void sendVerificationCode(String email, String type);
    void verifyEmail(String email, String code, String type);
    UserVO getUserInfo(Long userId);
    UserVO updateProfile(Long userId, UpdateProfileDTO dto);
    void changePassword(Long userId, String oldPassword, String newPassword);
    void resetPassword(String email, String code, String newPassword);
}
