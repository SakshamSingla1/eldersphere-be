package com.eldersphere.dtos.Admin;

import com.eldersphere.enums.UserTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GrantUserRoleRequest {
    @NotNull(message = "roleType is required")
    private UserTypeEnum roleType;
}
