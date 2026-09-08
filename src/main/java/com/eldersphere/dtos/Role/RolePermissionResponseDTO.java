package com.eldersphere.dtos.Role;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RolePermissionResponseDTO {
    private Long roleId;
    private String roleName;
    private List<PermissionResponseDTOLite> permissions;

    @Data
    @Builder
    public static class PermissionResponseDTOLite {
        private Long id;
        private String name;
    }
}
