package com.eldersphere.dtos.Permission;

import com.eldersphere.dtos.Common.AuditableResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PermissionResponseDTO extends AuditableResponse {
    private Long id;
    private String name;
    private String description;
}
