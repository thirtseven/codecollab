package com.codecollab.controller;

import com.codecollab.common.Result;
import com.codecollab.dto.LoginDTO;
import com.codecollab.dto.RegisterDTO;
import com.codecollab.dto.UpdateProfileDTO;
import com.codecollab.service.UserService;
import com.codecollab.vo.LoginVO;
import com.codecollab.vo.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        return Result.success(userService.login(dto, request.getRemoteAddr()));
    }

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.success(userService.register(dto));
    }

    @PostMapping("/send-code")
    public Result<Void> sendCode(@RequestParam String email, @RequestParam String type) {
        userService.sendVerificationCode(email, type);
        return Result.success();
    }

    @GetMapping("/verify-email")
    public Result<Void> verifyEmail(@RequestParam String email, @RequestParam String code, @RequestParam String type) {
        userService.verifyEmail(email, code, type);
        return Result.success();
    }

    @GetMapping("/user-info")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.getUserInfo(userId));
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(HttpServletRequest request, @RequestBody UpdateProfileDTO dto) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userService.updateProfile(userId, dto));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(HttpServletRequest request,
                                        @RequestParam String oldPassword,
                                        @RequestParam String newPassword) {
        Long userId = (Long) request.getAttribute("userId");
        userService.changePassword(userId, oldPassword, newPassword);
        return Result.success();
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestParam String email,
                                       @RequestParam String code,
                                       @RequestParam String newPassword) {
        userService.resetPassword(email, code, newPassword);
        return Result.success();
    }
}
