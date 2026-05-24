package com.syfe.finance_manager.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username (email) is required")
    @Email(message = "Please provide a valid email address")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
