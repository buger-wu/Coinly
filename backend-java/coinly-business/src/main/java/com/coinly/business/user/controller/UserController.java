package com.coinly.business.user.controller;

import com.coinly.business.user.dto.UpdatePasswordRequest;
import com.coinly.business.user.dto.UpdateProfileRequest;
import com.coinly.business.user.entity.UserEntity;
import com.coinly.business.user.service.UserService;
import com.coinly.common.context.UserContext;
import com.coinly.common.domain.CommonResponse;
import com.coinly.common.exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/v1/user")
public class UserController {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserController(UserService userService, BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/profile")
    public CommonResponse<UserEntity> getProfile() {
        Long userId = UserContext.getUserId();
        UserEntity user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return CommonResponse.success(user);
    }

    @PutMapping("/profile")
    public CommonResponse<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = UserContext.getUserId();
        UserEntity user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        user.setUpdateTime(LocalDateTime.now());

        userService.updateById(user);
        return CommonResponse.success("修改成功", null);
    }

    @PutMapping("/password")
    public CommonResponse<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        Long userId = UserContext.getUserId();
        UserEntity user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());

        userService.updateById(user);
        return CommonResponse.success("密码修改成功", null);
    }
}