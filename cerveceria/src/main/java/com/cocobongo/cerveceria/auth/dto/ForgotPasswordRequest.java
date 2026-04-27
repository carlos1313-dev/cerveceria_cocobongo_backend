package com.cocobongo.cerveceria.auth.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
/** POST /auth/forgot-password */
@Data
class ForgotPasswordRequest {
    @NotBlank
    @Email
    private String email;
}