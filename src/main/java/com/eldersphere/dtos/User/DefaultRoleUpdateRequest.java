package com.eldersphere.dtos.User;

import com.eldersphere.enums.UserTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DefaultRoleUpdateRequest {
    @NotNull(message = "roleType is required")
    private UserTypeEnum roleType;
}
