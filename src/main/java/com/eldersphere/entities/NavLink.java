package com.eldersphere.entities;

import com.eldersphere.audit.Auditable;
import com.eldersphere.enums.NavLinkStatusEnum;
import com.eldersphere.enums.UserTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "nav_links")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavLink extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserTypeEnum userType;

    @Column(name = "nav_group")
    private String navGroup;

    @Column(name = "nav_index", nullable = false)
    @Builder.Default
    private Integer navIndex = 0;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String path;

    private String icon;

    // Soft reference (matched by name) to permissions.name — see AdminPermissionGuard. Only
    // meaningful for user_type = ADMIN; null means "no fine-grained gate".
    @Column(name = "required_permission")
    private String requiredPermission;

    @Column(name = "super_admin_only", nullable = false)
    @Builder.Default
    private Boolean superAdminOnly = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NavLinkStatusEnum status = NavLinkStatusEnum.ACTIVE;
}
