package com.eldersphere.dtos.Role;

import com.eldersphere.dtos.Common.AuditableResponse;
import com.eldersphere.enums.RoleStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RoleResponseDTO extends AuditableResponse {
    private Long id;
    private String name;
    private String description;
    private RoleStatusEnum status;
}
