package com.eldersphere.dtos.Role;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RolePermissionRequestDTO {
    @NotNull(message = "Role ID is required")
    private Long roleId;
    @NotNull(message = "Permission ID is required")
    private Long permissionId;
}
