package com.eldersphere.dtos.Permission;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionRequestDTO {
    @NotBlank(message = "Permission name is required")
    private String name;
    private String description;
}
