package com.eldersphere.dtos.Authentication;

import com.eldersphere.enums.UserTypeEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponseDTO {
    private Long id;
    private String email;
    private String fullName;
    private UserTypeEnum userType;
}
