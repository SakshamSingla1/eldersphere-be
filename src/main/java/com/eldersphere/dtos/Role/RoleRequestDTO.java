package com.eldersphere.dtos.Role;

import com.eldersphere.enums.RoleStatusEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleRequestDTO {
    @NotBlank(message = "Role name is required")
    private String name;
    private String description;
    private RoleStatusEnum status;
}
