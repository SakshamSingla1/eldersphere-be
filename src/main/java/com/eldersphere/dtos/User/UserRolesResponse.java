package com.eldersphere.dtos.User;

import com.eldersphere.enums.UserTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRolesResponse {
    private Long userId;
    private List<UserTypeEnum> roles;
    private UserTypeEnum primaryRole;
}
