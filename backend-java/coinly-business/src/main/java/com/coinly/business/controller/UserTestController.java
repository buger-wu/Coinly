package com.coinly.business.controller;

import com.coinly.business.user.entity.UserEntity;
import com.coinly.business.user.service.UserService;
import com.coinly.common.domain.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/users")
public class UserTestController {

    private final UserService userService;

    public UserTestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public CommonResponse<UserEntity> getById(@PathVariable Long id) {
        UserEntity user = userService.getById(id);
        if (user == null) {
            return CommonResponse.fail("user not found");
        }
        return CommonResponse.success(user);
    }
}
