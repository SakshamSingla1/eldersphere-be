package com.eldersphere.security;

import com.eldersphere.entities.Permission;
import com.eldersphere.entities.Role;
import com.eldersphere.entities.User;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.repositories.PermissionRepository;
import com.eldersphere.repositories.RolePermissionRepository;
import com.eldersphere.repositories.RoleRepository;
import com.eldersphere.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Wires the pre-existing (previously dormant) Role/Permission/RolePermission fine-grained
 * admin RBAC system into real enforcement, used from {@code @PreAuthorize} SpEL expressions
 * as {@code @adminPermissionGuard.has('SOME_PERMISSION_NAME')}.
 *
 * <p><b>This check only ever narrows access for an {@code ADMIN} user who has been assigned a
 * {@code roleId}.</b> It is a deliberate no-op — always returns {@code true} — for:
 * <ul>
 *   <li>anyone who isn't authenticated the way the surrounding {@code @PreAuthorize} clause
 *       already expects (that clause, e.g. {@code hasRole('ADMIN')} or
 *       {@code isAuthenticated()}, still runs first and is what actually blocks them);</li>
 *   <li>{@code SUPER_ADMIN} users, who always have full access regardless of {@code roleId};</li>
 *   <li>any non-{@code ADMIN}-tier user (FAMILY_MEMBER/CARETAKER/ELDER) reaching a shared,
 *       non-admin-only endpoint this guard has also been added to;</li>
 *   <li>an {@code ADMIN} user whose {@code roleId} is {@code null} — today's default for
 *       every existing admin account, so nobody already using this app loses access.</li>
 * </ul>
 * Only when the caller is an {@code ADMIN} with a non-null {@code roleId} does this evaluate
 * the assigned {@link Role}'s {@link Permission} set for real.
 */
@Component("adminPermissionGuard")
@RequiredArgsConstructor
public class AdminPermissionGuard {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public boolean has(String permissionName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(String.valueOf(authentication.getPrincipal()))) {
            // No authenticated principal to check - defer entirely to the surrounding
            // @PreAuthorize clause (isAuthenticated()/hasRole(...)), which will deny this.
            return true;
        }

        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (user == null || user.getUserType() != UserTypeEnum.ADMIN) {
            // SUPER_ADMIN and every non-ADMIN-tier user are unaffected by this guard.
            return true;
        }

        Long roleId = user.getRoleId();
        if (roleId == null) {
            // Legacy/default state: an ADMIN with no fine-grained role assigned keeps full
            // admin access exactly as before this system was wired up.
            return true;
        }

        Role role = roleRepository.findById(roleId).orElse(null);
        if (role == null) {
            // Dangling roleId (role deleted after assignment) - fail open rather than
            // locking the admin out entirely; an admin should reassign/clear the role.
            return true;
        }

        Permission permission = permissionRepository.findByName(permissionName).orElse(null);
        if (permission == null) {
            // Required permission key isn't in the catalog - nothing to grant it against.
            return false;
        }

        return rolePermissionRepository.existsByRoleIdAndPermissionId(role.getId(), permission.getId());
    }
}
