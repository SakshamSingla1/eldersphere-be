package com.eldersphere.security;

import com.eldersphere.entities.Permission;
import com.eldersphere.entities.Role;
import com.eldersphere.entities.User;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.repositories.PermissionRepository;
import com.eldersphere.repositories.RolePermissionRepository;
import com.eldersphere.repositories.RoleRepository;
import com.eldersphere.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AdminPermissionGuard}, the enforcement point that wires the
 * previously-dormant Role/Permission/RolePermission RBAC system into real endpoint access
 * control. Confirms the guard is a pure no-op (always grants) for everyone except an ADMIN
 * who has been assigned a roleId - i.e. that turning this on cannot retroactively lock out
 * any existing admin account.
 */
class AdminPermissionGuardTest {

    private static final String PERMISSION = "BOOKINGS_VIEW";

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PermissionRepository permissionRepository;
    private RolePermissionRepository rolePermissionRepository;
    private AdminPermissionGuard guard;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        roleRepository = mock(RoleRepository.class);
        permissionRepository = mock(PermissionRepository.class);
        rolePermissionRepository = mock(RolePermissionRepository.class);
        guard = new AdminPermissionGuard(userRepository, roleRepository, permissionRepository, rolePermissionRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String email) {
        // The 2-arg constructor marks the token unauthenticated (Spring Security convention),
        // which would make AdminPermissionGuard's own "not authenticated -> defer" short-circuit
        // mask everything below it - use the authorities constructor so isAuthenticated() is true,
        // matching what a real, already-JWT-authenticated request looks like.
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, "password", java.util.List.of()));
    }

    @Test
    void noAuthentication_defersToSurroundingPreAuthorizeClause_returnsTrue() {
        SecurityContextHolder.clearContext();
        assertThat(guard.has(PERMISSION)).isTrue();
    }

    @Test
    void anonymousAuthentication_returnsTrue() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("anonymousUser", "n/a"));
        assertThat(guard.has(PERMISSION)).isTrue();
    }

    @Test
    void superAdmin_alwaysGranted_regardlessOfRoleId() {
        authenticateAs("superadmin@eldersphere.app");
        User superAdmin = User.builder().id(1L).userType(UserTypeEnum.SUPER_ADMIN).roleId(999L).build();
        when(userRepository.findByEmail("superadmin@eldersphere.app")).thenReturn(Optional.of(superAdmin));

        assertThat(guard.has(PERMISSION)).isTrue();
    }

    @Test
    void nonAdminTierUser_alwaysGranted_guardIsNoOpForSharedEndpoints() {
        authenticateAs("family@eldersphere.app");
        User familyMember = User.builder().id(2L).userType(UserTypeEnum.FAMILY_MEMBER).build();
        when(userRepository.findByEmail("family@eldersphere.app")).thenReturn(Optional.of(familyMember));

        assertThat(guard.has(PERMISSION)).isTrue();
    }

    @Test
    void adminWithNoRoleId_keepsFullLegacyAccess() {
        authenticateAs("admin@eldersphere.app");
        User admin = User.builder().id(3L).userType(UserTypeEnum.ADMIN).roleId(null).build();
        when(userRepository.findByEmail("admin@eldersphere.app")).thenReturn(Optional.of(admin));

        assertThat(guard.has(PERMISSION)).isTrue();
    }

    @Test
    void adminWithRoleId_roleHasPermission_grantsAccess() {
        authenticateAs("restricted-admin@eldersphere.app");
        User admin = User.builder().id(4L).userType(UserTypeEnum.ADMIN).roleId(10L).build();
        Role role = Role.builder().id(10L).name("Bookings Coordinator").build();
        Permission permission = Permission.builder().id(20L).name(PERMISSION).build();
        when(userRepository.findByEmail("restricted-admin@eldersphere.app")).thenReturn(Optional.of(admin));
        when(roleRepository.findById(10L)).thenReturn(Optional.of(role));
        when(permissionRepository.findByName(PERMISSION)).thenReturn(Optional.of(permission));
        when(rolePermissionRepository.existsByRoleIdAndPermissionId(10L, 20L)).thenReturn(true);

        assertThat(guard.has(PERMISSION)).isTrue();
    }

    @Test
    void adminWithRoleId_roleLacksPermission_denies() {
        authenticateAs("restricted-admin@eldersphere.app");
        User admin = User.builder().id(4L).userType(UserTypeEnum.ADMIN).roleId(10L).build();
        Role role = Role.builder().id(10L).name("Content Manager").build();
        Permission permission = Permission.builder().id(20L).name(PERMISSION).build();
        when(userRepository.findByEmail("restricted-admin@eldersphere.app")).thenReturn(Optional.of(admin));
        when(roleRepository.findById(10L)).thenReturn(Optional.of(role));
        when(permissionRepository.findByName(PERMISSION)).thenReturn(Optional.of(permission));
        when(rolePermissionRepository.existsByRoleIdAndPermissionId(10L, 20L)).thenReturn(false);

        assertThat(guard.has(PERMISSION)).isFalse();
    }

    @Test
    void adminWithDanglingRoleId_failsOpen() {
        authenticateAs("restricted-admin@eldersphere.app");
        User admin = User.builder().id(4L).userType(UserTypeEnum.ADMIN).roleId(999L).build();
        when(userRepository.findByEmail("restricted-admin@eldersphere.app")).thenReturn(Optional.of(admin));
        when(roleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThat(guard.has(PERMISSION)).isTrue();
    }

    @Test
    void unknownPermissionKey_denies() {
        authenticateAs("restricted-admin@eldersphere.app");
        User admin = User.builder().id(4L).userType(UserTypeEnum.ADMIN).roleId(10L).build();
        Role role = Role.builder().id(10L).name("Content Manager").build();
        when(userRepository.findByEmail("restricted-admin@eldersphere.app")).thenReturn(Optional.of(admin));
        when(roleRepository.findById(10L)).thenReturn(Optional.of(role));
        when(permissionRepository.findByName("NOT_A_REAL_PERMISSION")).thenReturn(Optional.empty());

        assertThat(guard.has("NOT_A_REAL_PERMISSION")).isFalse();
    }
}
