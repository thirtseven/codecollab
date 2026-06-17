package com.codecollab.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codecollab.common.Constants;
import com.codecollab.dto.LoginDTO;
import com.codecollab.dto.RegisterDTO;
import com.codecollab.dto.UpdateProfileDTO;
import com.codecollab.entity.EmailVerification;
import com.codecollab.entity.User;
import com.codecollab.exception.BusinessException;
import com.codecollab.mapper.EmailVerificationMapper;
import com.codecollab.mapper.UserMapper;
import com.codecollab.service.UserService;
import com.codecollab.util.JwtUtil;
import com.codecollab.vo.LoginVO;
import com.codecollab.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailVerificationMapper emailVerificationMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Override
    public LoginVO login(LoginDTO dto, String ip) {
        User user = userMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        if (user.getStatus() == Constants.STATUS_BANNED) {
            throw new BusinessException(403, "账号已被封禁");
        }
        if (user.getStatus() == Constants.STATUS_INACTIVE) {
            throw new BusinessException(403, "账号已被禁用");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userMapper.updateById(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setRefreshToken(refreshToken);
        loginVO.setUser(convertToVO(user));
        return loginVO;
    }

    @Override
    @Transactional
    public UserVO register(RegisterDTO dto) {
        if (userMapper.selectByUsername(dto.getUsername()) != null) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 验证邮箱验证码
        EmailVerification verification = emailVerificationMapper.findValidCode(
            dto.getEmail(), Constants.EMAIL_TYPE_REGISTER, LocalDateTime.now());
        if (verification == null || !verification.getCode().equals(dto.getCode())) {
            throw new BusinessException(400, "验证码无效或已过期");
        }
        verification.setUsed(1);
        emailVerificationMapper.updateById(verification);

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setRole(Constants.ROLE_STUDENT);
        user.setStatus(Constants.STATUS_ACTIVE);
        userMapper.insert(user);
        return convertToVO(user);
    }

    @Override
    @Transactional
    public UserVO adminRegister(RegisterDTO dto) {
        if (userMapper.selectByUsername(dto.getUsername()) != null) {
            throw new BusinessException(400, "用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setRole(dto.getRole() != null ? dto.getRole() : Constants.ROLE_STUDENT);
        user.setStatus(Constants.STATUS_ACTIVE);
        userMapper.insert(user);
        return convertToVO(user);
    }

    @Override
    public void sendVerificationCode(String email, String type) {
        LambdaQueryWrapper<EmailVerification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmailVerification::getEmail, email)
            .eq(EmailVerification::getType, type)
            .eq(EmailVerification::getUsed, 0)
            .gt(EmailVerification::getExpireTime, LocalDateTime.now())
            .orderByDesc(EmailVerification::getCreatedAt)
            .last("LIMIT 1");
        EmailVerification existing = emailVerificationMapper.selectOne(wrapper);
        if (existing != null) {
            long seconds = java.time.Duration.between(LocalDateTime.now(), existing.getExpireTime()).getSeconds();
            throw new BusinessException(429, "请等待 " + seconds + " 秒后再请求新验证码");
        }

        String code = RandomUtil.randomNumbers(6);
        EmailVerification verification = new EmailVerification();
        verification.setEmail(email);
        verification.setCode(code);
        verification.setType(type);
        verification.setExpireTime(LocalDateTime.now().plusMinutes(5));
        verification.setUsed(0);
        emailVerificationMapper.insert(verification);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(email);
            message.setSubject("CodeCollab - 验证码");
            message.setText("您的验证码是: " + code + "\n此验证码5分钟内有效。\n如非本人操作，请忽略此邮件。");
            mailSender.send(message);
        } catch (Exception e) {
            throw new BusinessException(500, "验证码发送失败: " + e.getMessage());
        }
    }

    @Override
    public void verifyEmail(String email, String code, String type) {
        EmailVerification verification = emailVerificationMapper.findValidCode(email, type, LocalDateTime.now());
        if (verification == null || !verification.getCode().equals(code)) {
            throw new BusinessException(400, "验证码无效或已过期");
        }
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return convertToVO(user);
    }

    @Override
    public UserVO updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getBio() != null) user.setBio(dto.getBio());
        if (dto.getGithubUrl() != null) user.setGithubUrl(dto.getGithubUrl());
        if (dto.getWebsite() != null) user.setWebsite(dto.getWebsite());
        userMapper.updateById(user);
        return convertToVO(user);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(400, "原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        EmailVerification verification = emailVerificationMapper.findValidCode(email, Constants.EMAIL_TYPE_RESET, LocalDateTime.now());
        if (verification == null || !verification.getCode().equals(code)) {
            throw new BusinessException(400, "验证码无效或已过期");
        }
        verification.setUsed(1);
        emailVerificationMapper.updateById(verification);

        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new BusinessException(404, "邮箱未注册");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
