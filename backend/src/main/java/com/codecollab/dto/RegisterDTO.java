package com.codecollab.dto;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class RegisterDTO {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be 3-20 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 20, message = "Password must be 6-20 characters")
    private String password;

    @NotBlank(message = "Verification code is required")
    private String code;

    private String nickname;
}
