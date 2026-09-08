package com.eldersphere.dtos.User;

import com.eldersphere.dtos.Common.AuditableResponse;
import com.eldersphere.enums.UserStatusEnum;
import com.eldersphere.enums.UserTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserResponse extends AuditableResponse {
    private Long id;
    private String email;
    private String phone;
    private String fullName;
    private UserTypeEnum userType;
    private UserStatusEnum status;
    private Long roleId;
    private String roleName;
    /** Full set of UserTypeEnum roles this user holds (see user_role_mappings); userType above is the primary/default one. */
    private List<UserTypeEnum> roles;
}
