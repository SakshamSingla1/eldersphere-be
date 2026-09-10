package com.eldersphere.dtos.NavLink;

import com.eldersphere.dtos.Common.AuditableResponse;
import com.eldersphere.enums.NavLinkStatusEnum;
import com.eldersphere.enums.UserTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NavLinkResponse extends AuditableResponse {
    private Long id;
    private UserTypeEnum userType;
    private String navGroup;
    private Integer navIndex;
    private String name;
    private String path;
    private String icon;
    private String requiredPermission;
    private Boolean superAdminOnly;
    private NavLinkStatusEnum status;
}
