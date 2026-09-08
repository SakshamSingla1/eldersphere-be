package com.eldersphere.entities;

import com.eldersphere.audit.Auditable;
import com.eldersphere.enums.UserTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * A single role held by a user. A user can hold several of these at once
 * (e.g. FAMILY_MEMBER + CARETAKER); exactly one of them has
 * {@code isPrimary = true}, whose {@code roleType} must always match
 * {@link User#getUserType()}.
 *
 * <p>NOT related to the pre-existing {@link Role}/{@link Permission}/
 * {@link RolePermission} fine-grained RBAC system used for admin-panel
 * nav-item visibility — this table is named "mapping" specifically to
 * avoid being confused with that system.
 */
@Entity
@Table(name = "user_role_mappings")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleMapping extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false)
    private UserTypeEnum roleType;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary;

    @Column(name = "granted_by_user_id")
    private Long grantedByUserId;
}
