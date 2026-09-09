package com.eldersphere.dtos.Authentication;

import com.eldersphere.dtos.ColorTheme.UserThemeResponseDTO;
import com.eldersphere.enums.UserStatusEnum;
import com.eldersphere.enums.UserTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginResponseDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    /** Primary/default role — the role the user lands on after login. Unchanged field, still single-valued. */
    private UserTypeEnum userType;
    private UserStatusEnum status;
    private Long roleId;
    private String roleName;
    /** Full set of UserTypeEnum roles this user holds. */
    private List<UserTypeEnum> roles;
    private String token;
    /** The caller's resolved active color theme (their own pick, or the default if unset) -
     * included inline so the frontend can paint its dashboard theme immediately after login
     * without a second round-trip to GET /users/me/theme. */
    private UserThemeResponseDTO activeTheme;
}
