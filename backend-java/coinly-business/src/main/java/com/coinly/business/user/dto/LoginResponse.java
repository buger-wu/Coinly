package com.coinly.business.user.dto;

import lombok.Data;

@Data
public class LoginResponse {

    private Long id;
    private String username;
    private String nickname;
    private String token;

    public LoginResponse(Long id, String username, String nickname, String token) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.token = token;
    }
}