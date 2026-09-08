package com.eldersphere.dtos.Authentication;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PasswordResetRequestDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;
}
