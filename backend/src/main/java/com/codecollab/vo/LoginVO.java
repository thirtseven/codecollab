package com.codecollab.vo;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private String refreshToken;
    private UserVO user;
}
