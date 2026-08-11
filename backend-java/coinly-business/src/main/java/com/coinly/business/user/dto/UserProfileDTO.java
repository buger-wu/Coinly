package com.coinly.business.user.dto;

import lombok.Data;

/**
 * 用户个人信息 DTO。
 *
 * <p>不包含 password 等敏感字段，用于 /v1/user/profile 返回。
 */
@Data
public class UserProfileDTO {

    private Long id;

    private String username;

    private String nickname;

    private String email;
}
