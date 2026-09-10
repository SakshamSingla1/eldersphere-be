package com.eldersphere.dtos.NavLink;

import com.eldersphere.enums.NavLinkStatusEnum;
import com.eldersphere.enums.UserTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NavLinkRequest {
    @NotNull(message = "Portal (user type) is required")
    private UserTypeEnum userType;

    private String navGroup;

    @NotNull(message = "Order is required")
    private Integer navIndex;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Path is required")
    private String path;

    private String icon;

    private String requiredPermission;

    private Boolean superAdminOnly;

    private NavLinkStatusEnum status;
}
