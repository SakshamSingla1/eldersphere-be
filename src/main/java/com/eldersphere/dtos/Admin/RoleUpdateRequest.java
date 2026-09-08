package com.eldersphere.dtos.Admin;

import lombok.Builder;
import lombok.Data;

/**
 * roleId is intentionally nullable here (no @NotNull) - the admin-facing "assign a role"
 * UI also needs to be able to clear a restricted admin back to unrestricted access
 * (roleId = null), which AdminServiceImpl#assignRole already supports at the service
 * layer. The request must still explicitly include the "roleId" key (even as null);
 * omitting the field entirely also deserializes to null, so both forms work.
 */
@Data
@Builder
public class RoleUpdateRequest {
    private Long roleId;
}
