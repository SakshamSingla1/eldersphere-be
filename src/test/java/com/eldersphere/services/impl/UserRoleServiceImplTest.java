package com.eldersphere.services.impl;

import com.eldersphere.dao.user.UserDao;
import com.eldersphere.dao.user.UserRoleMappingDao;
import com.eldersphere.dtos.User.UserRolesResponse;
import com.eldersphere.entities.User;
import com.eldersphere.entities.UserRoleMapping;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.utils.Helper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the multi-role assignment rules in {@link UserRoleServiceImpl}: a user's
 * last remaining role can never be revoked, their current primary role can't be revoked
 * without switching away from it first, and granting/revoking ADMIN/SUPER_ADMIN requires the
 * caller to already be a SUPER_ADMIN.
 */
class UserRoleServiceImplTest {

    private static final Long TARGET_USER_ID = 42L;

    private UserDao userDao;
    private UserRoleMappingDao userRoleMappingDao;
    private Helper helper;
    private UserRoleServiceImpl userRoleService;

    @BeforeEach
    void setUp() {
        userDao = mock(UserDao.class);
        userRoleMappingDao = mock(UserRoleMappingDao.class);
        helper = mock(Helper.class);
        userRoleService = new UserRoleServiceImpl(userDao, userRoleMappingDao, helper);
    }

    private User userWithType(Long id, UserTypeEnum type) {
        return User.builder().id(id).userType(type).build();
    }

    // ---------------------------------------------------------------
    // revokeRole: last-role and primary-role guards
    // ---------------------------------------------------------------

    @Test
    void revokeRole_lastRemainingRole_throwsLastUserRoleCannotBeRevoked() {
        when(userDao.findById(eq(TARGET_USER_ID), eq(true))).thenReturn(userWithType(TARGET_USER_ID, UserTypeEnum.FAMILY_MEMBER));
        UserRoleMapping mapping = UserRoleMapping.builder().userId(TARGET_USER_ID).roleType(UserTypeEnum.FAMILY_MEMBER).isPrimary(true).build();
        when(userRoleMappingDao.find(TARGET_USER_ID, UserTypeEnum.FAMILY_MEMBER)).thenReturn(Optional.of(mapping));
        when(userRoleMappingDao.countByUserId(TARGET_USER_ID)).thenReturn(1L);

        GenericException ex = assertThrows(GenericException.class,
                () -> userRoleService.revokeRole(TARGET_USER_ID, UserTypeEnum.FAMILY_MEMBER));

        assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCodeEnum.LAST_USER_ROLE_CANNOT_BE_REVOKED);
        verify(userRoleMappingDao, never()).delete(any());
    }

    @Test
    void revokeRole_currentPrimaryRole_withMultipleRolesHeld_throwsPrimaryCannotBeRevoked() {
        when(userDao.findById(eq(TARGET_USER_ID), eq(true))).thenReturn(userWithType(TARGET_USER_ID, UserTypeEnum.FAMILY_MEMBER));
        UserRoleMapping mapping = UserRoleMapping.builder().userId(TARGET_USER_ID).roleType(UserTypeEnum.FAMILY_MEMBER).isPrimary(true).build();
        when(userRoleMappingDao.find(TARGET_USER_ID, UserTypeEnum.FAMILY_MEMBER)).thenReturn(Optional.of(mapping));
        when(userRoleMappingDao.countByUserId(TARGET_USER_ID)).thenReturn(2L);

        GenericException ex = assertThrows(GenericException.class,
                () -> userRoleService.revokeRole(TARGET_USER_ID, UserTypeEnum.FAMILY_MEMBER));

        assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCodeEnum.PRIMARY_USER_ROLE_CANNOT_BE_REVOKED);
        verify(userRoleMappingDao, never()).delete(any());
    }

    @Test
    void revokeRole_nonPrimarySecondaryRole_withMultipleRolesHeld_succeeds() throws GenericException {
        when(userDao.findById(eq(TARGET_USER_ID), eq(true))).thenReturn(userWithType(TARGET_USER_ID, UserTypeEnum.FAMILY_MEMBER));
        UserRoleMapping mapping = UserRoleMapping.builder().userId(TARGET_USER_ID).roleType(UserTypeEnum.CARETAKER).isPrimary(false).build();
        when(userRoleMappingDao.find(TARGET_USER_ID, UserTypeEnum.CARETAKER)).thenReturn(Optional.of(mapping));
        when(userRoleMappingDao.countByUserId(TARGET_USER_ID)).thenReturn(2L);
        when(userRoleMappingDao.findByUserId(TARGET_USER_ID)).thenReturn(List.of(
                UserRoleMapping.builder().userId(TARGET_USER_ID).roleType(UserTypeEnum.FAMILY_MEMBER).isPrimary(true).build()));

        UserRolesResponse response = userRoleService.revokeRole(TARGET_USER_ID, UserTypeEnum.CARETAKER);

        assertThat(response.getRoles()).containsExactly(UserTypeEnum.FAMILY_MEMBER);
        verify(userRoleMappingDao).delete(mapping);
    }

    @Test
    void revokeRole_roleNotAssigned_throwsUserRoleNotAssigned() {
        when(userDao.findById(eq(TARGET_USER_ID), eq(true))).thenReturn(userWithType(TARGET_USER_ID, UserTypeEnum.FAMILY_MEMBER));
        when(userRoleMappingDao.find(TARGET_USER_ID, UserTypeEnum.CARETAKER)).thenReturn(Optional.empty());

        GenericException ex = assertThrows(GenericException.class,
                () -> userRoleService.revokeRole(TARGET_USER_ID, UserTypeEnum.CARETAKER));

        assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCodeEnum.USER_ROLE_NOT_ASSIGNED);
    }

    // ---------------------------------------------------------------
    // SUPER_ADMIN-only gate for granting/revoking ADMIN/SUPER_ADMIN
    // ---------------------------------------------------------------

    @Test
    void grantRole_adminRole_byNonSuperAdminCaller_throwsForbidden() throws GenericException {
        when(userDao.findById(eq(TARGET_USER_ID), eq(true))).thenReturn(userWithType(TARGET_USER_ID, UserTypeEnum.FAMILY_MEMBER));
        when(helper.getAuthenticatedUser()).thenReturn(userWithType(1L, UserTypeEnum.ADMIN));

        GenericException ex = assertThrows(GenericException.class,
                () -> userRoleService.grantRole(TARGET_USER_ID, UserTypeEnum.ADMIN));

        assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCodeEnum.FORBIDDEN);
        verify(userRoleMappingDao, never()).save(any());
    }

    @Test
    void grantRole_adminRole_bySuperAdminCaller_succeeds() throws GenericException {
        when(userDao.findById(eq(TARGET_USER_ID), eq(true))).thenReturn(userWithType(TARGET_USER_ID, UserTypeEnum.FAMILY_MEMBER));
        when(helper.getAuthenticatedUser()).thenReturn(userWithType(1L, UserTypeEnum.SUPER_ADMIN));
        when(helper.getAuthenticatedUserId()).thenReturn(1L);
        when(userRoleMappingDao.exists(TARGET_USER_ID, UserTypeEnum.ADMIN)).thenReturn(false);
        when(userRoleMappingDao.findByUserId(TARGET_USER_ID)).thenReturn(List.of(
                UserRoleMapping.builder().userId(TARGET_USER_ID).roleType(UserTypeEnum.FAMILY_MEMBER).isPrimary(true).build(),
                UserRoleMapping.builder().userId(TARGET_USER_ID).roleType(UserTypeEnum.ADMIN).isPrimary(false).build()));

        UserRolesResponse response = userRoleService.grantRole(TARGET_USER_ID, UserTypeEnum.ADMIN);

        assertThat(response.getRoles()).containsExactlyInAnyOrder(UserTypeEnum.FAMILY_MEMBER, UserTypeEnum.ADMIN);
        verify(userRoleMappingDao).save(any());
    }

    @Test
    void grantRole_nonAdminRole_doesNotRequireSuperAdminCheck() throws GenericException {
        when(userDao.findById(eq(TARGET_USER_ID), eq(true))).thenReturn(userWithType(TARGET_USER_ID, UserTypeEnum.FAMILY_MEMBER));
        when(userRoleMappingDao.exists(TARGET_USER_ID, UserTypeEnum.CARETAKER)).thenReturn(false);
        when(helper.getAuthenticatedUserId()).thenReturn(1L);
        when(userRoleMappingDao.findByUserId(TARGET_USER_ID)).thenReturn(List.of(
                UserRoleMapping.builder().userId(TARGET_USER_ID).roleType(UserTypeEnum.FAMILY_MEMBER).isPrimary(true).build()));

        userRoleService.grantRole(TARGET_USER_ID, UserTypeEnum.CARETAKER);

        verify(helper, never()).getAuthenticatedUser();
    }

    @Test
    void grantRole_alreadyAssigned_throwsUserRoleAlreadyAssigned() {
        when(userDao.findById(eq(TARGET_USER_ID), eq(true))).thenReturn(userWithType(TARGET_USER_ID, UserTypeEnum.FAMILY_MEMBER));
        when(userRoleMappingDao.exists(TARGET_USER_ID, UserTypeEnum.CARETAKER)).thenReturn(true);

        GenericException ex = assertThrows(GenericException.class,
                () -> userRoleService.grantRole(TARGET_USER_ID, UserTypeEnum.CARETAKER));

        assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCodeEnum.USER_ROLE_ALREADY_ASSIGNED);
    }

    @Test
    void revokeRole_superAdminRole_byNonSuperAdminCaller_throwsForbidden() throws GenericException {
        when(userDao.findById(eq(TARGET_USER_ID), eq(true))).thenReturn(userWithType(TARGET_USER_ID, UserTypeEnum.SUPER_ADMIN));
        when(helper.getAuthenticatedUser()).thenReturn(userWithType(1L, UserTypeEnum.ADMIN));

        GenericException ex = assertThrows(GenericException.class,
                () -> userRoleService.revokeRole(TARGET_USER_ID, UserTypeEnum.SUPER_ADMIN));

        assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCodeEnum.FORBIDDEN);
        verify(userRoleMappingDao, never()).delete(any());
    }

    // ---------------------------------------------------------------
    // switchDefaultRole
    // ---------------------------------------------------------------

    @Test
    void switchDefaultRole_roleNotHeld_throwsUserRoleNotHeld() {
        when(userDao.findById(eq(TARGET_USER_ID), eq(true))).thenReturn(userWithType(TARGET_USER_ID, UserTypeEnum.FAMILY_MEMBER));
        when(userRoleMappingDao.findByUserId(TARGET_USER_ID)).thenReturn(List.of(
                UserRoleMapping.builder().userId(TARGET_USER_ID).roleType(UserTypeEnum.FAMILY_MEMBER).isPrimary(true).build()));

        GenericException ex = assertThrows(GenericException.class,
                () -> userRoleService.switchDefaultRole(TARGET_USER_ID, UserTypeEnum.CARETAKER));

        assertThat(ex.getExceptionCode()).isEqualTo(ExceptionCodeEnum.USER_ROLE_NOT_HELD);
        verify(userDao, never()).save(any());
    }

    @Test
    void switchDefaultRole_roleHeld_flipsPrimaryFlagsAndUserType() throws GenericException {
        User user = userWithType(TARGET_USER_ID, UserTypeEnum.FAMILY_MEMBER);
        when(userDao.findById(eq(TARGET_USER_ID), eq(true))).thenReturn(user);
        UserRoleMapping familyMapping = UserRoleMapping.builder().userId(TARGET_USER_ID).roleType(UserTypeEnum.FAMILY_MEMBER).isPrimary(true).build();
        UserRoleMapping elderMapping = UserRoleMapping.builder().userId(TARGET_USER_ID).roleType(UserTypeEnum.ELDER).isPrimary(false).build();
        when(userRoleMappingDao.findByUserId(TARGET_USER_ID)).thenReturn(List.of(familyMapping, elderMapping));

        UserRolesResponse response = userRoleService.switchDefaultRole(TARGET_USER_ID, UserTypeEnum.ELDER);

        assertThat(familyMapping.isPrimary()).isFalse();
        assertThat(elderMapping.isPrimary()).isTrue();
        assertThat(user.getUserType()).isEqualTo(UserTypeEnum.ELDER);
        assertThat(response.getPrimaryRole()).isEqualTo(UserTypeEnum.ELDER);
        verify(userDao).save(user);
    }
}
