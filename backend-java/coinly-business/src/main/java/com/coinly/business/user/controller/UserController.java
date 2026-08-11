package com.coinly.business.user.controller;

import com.coinly.business.user.dto.UpdatePasswordRequest;
import com.coinly.business.user.dto.UpdateProfileRequest;
import com.coinly.business.user.dto.UserProfileDTO;
import com.coinly.business.user.entity.UserEntity;
import com.coinly.business.user.service.UserService;
import com.coinly.common.context.UserContext;
import com.coinly.common.domain.CommonResponse;
import com.coinly.common.exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;

@Tag(name = "用户模块")
@RestController
@RequestMapping("/v1/user")
public class UserController {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserController(UserService userService, BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "获取个人信息")
    @GetMapping("/profile")
    public CommonResponse<UserProfileDTO> getProfile() {
        Long userId = UserContext.getUserId();
        UserEntity user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        return CommonResponse.success(dto);
    }

    @Operation(summary = "修改基本信息")
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

    @Operation(summary = "修改密码")
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