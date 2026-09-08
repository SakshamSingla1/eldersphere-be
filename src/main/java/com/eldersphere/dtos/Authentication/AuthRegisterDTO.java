package com.eldersphere.dtos.Authentication;

import com.eldersphere.enums.UserTypeEnum;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthRegisterDTO {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9]).{8,}$", message = "Password must contain at least one uppercase letter and one digit")
    private String password;

    @NotNull(message = "User type is required")
    private UserTypeEnum userType;
}
