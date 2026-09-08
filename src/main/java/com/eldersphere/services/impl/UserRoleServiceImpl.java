package com.eldersphere.services.impl;

import com.eldersphere.dao.user.UserDao;
import com.eldersphere.dao.user.UserRoleMappingDao;
import com.eldersphere.dtos.User.UserRolesResponse;
import com.eldersphere.entities.User;
import com.eldersphere.entities.UserRoleMapping;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.UserRoleService;
import com.eldersphere.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    // Same admin-tier gate used by AdminServiceImpl: granting/revoking
    // ADMIN or SUPER_ADMIN role types requires the caller to already be a
    // SUPER_ADMIN. Granting/revoking ELDER/CARETAKER/FAMILY_MEMBER just
    // requires ADMIN or SUPER_ADMIN, which the controller-level
    // @PreAuthorize("hasRole('ADMIN')") on AdminController already covers
    // (the ROLE_SUPER_ADMIN > ROLE_ADMIN hierarchy lets SUPER_ADMIN callers
    // through that check too).
    private static final Set<UserTypeEnum> ADMIN_TIER = EnumSet.of(UserTypeEnum.ADMIN, UserTypeEnum.SUPER_ADMIN);

    private final UserDao userDao;
    private final UserRoleMappingDao userRoleMappingDao;
    private final Helper helper;

    @Override
    public List<UserTypeEnum> getRoleTypes(Long userId) {
        List<UserTypeEnum> roles = userRoleMappingDao.findByUserId(userId).stream()
                .map(UserRoleMapping::getRoleType)
                .distinct()
                .toList();
        if (!roles.isEmpty()) {
            return roles;
        }
        // Safety net: a user somehow without any mapping row falls back to
        // their primary users.user_type so authorization never silently
        // degrades to "no roles at all".
        User user = userDao.findById(userId, true);
        return (user != null && user.getUserType() != null) ? List.of(user.getUserType()) : List.of();
    }

    @Override
    @Transactional
    public void ensurePrimaryMapping(User user, Long grantedByUserId) {
        if (userRoleMappingDao.exists(user.getId(), user.getUserType())) {
            return;
        }
        userRoleMappingDao.save(UserRoleMapping.builder()
                .userId(user.getId())
                .roleType(user.getUserType())
                .isPrimary(true)
                .grantedByUserId(grantedByUserId)
                .build());
    }

    @Override
    public UserRolesResponse getMyRoles(Long userId) throws GenericException {
        return buildResponse(userId);
    }

    @Override
    @Transactional
    public UserRolesResponse switchDefaultRole(Long userId, UserTypeEnum newPrimaryRole) throws GenericException {
        User user = userDao.findById(userId, true);
        if (user == null) {
            throw new GenericException(ExceptionCodeEnum.USER_NOT_FOUND, "User not found");
        }
        List<UserRoleMapping> mappings = userRoleMappingDao.findByUserId(userId);
        boolean holdsRole = mappings.stream().anyMatch(m -> m.getRoleType() == newPrimaryRole);
        if (!holdsRole) {
            throw new GenericException(ExceptionCodeEnum.USER_ROLE_NOT_HELD,
                    "You do not hold the " + newPrimaryRole + " role, so it cannot be set as your default");
        }
        for (UserRoleMapping mapping : mappings) {
            boolean shouldBePrimary = mapping.getRoleType() == newPrimaryRole;
            if (mapping.isPrimary() != shouldBePrimary) {
                mapping.setPrimary(shouldBePrimary);
                userRoleMappingDao.save(mapping);
            }
        }
        user.setUserType(newPrimaryRole);
        userDao.save(user);
        return buildResponse(userId);
    }

    @Override
    @Transactional
    public UserRolesResponse grantRole(Long targetUserId, UserTypeEnum roleType) throws GenericException {
        User target = userDao.findById(targetUserId, true);
        if (target == null) {
            throw new GenericException(ExceptionCodeEnum.USER_NOT_FOUND, "User not found");
        }
        if (ADMIN_TIER.contains(roleType)) {
            requireSuperAdmin();
        }
        if (userRoleMappingDao.exists(targetUserId, roleType)) {
            throw new GenericException(ExceptionCodeEnum.USER_ROLE_ALREADY_ASSIGNED,
                    "User already holds the " + roleType + " role");
        }
        Long grantedByUserId = null;
        try {
            grantedByUserId = helper.getAuthenticatedUserId();
        } catch (GenericException ignored) {
            // Not authenticated somehow despite passing the controller's
            // @PreAuthorize check — leave grantedByUserId null rather than
            // failing the grant.
        }
        userRoleMappingDao.save(UserRoleMapping.builder()
                .userId(targetUserId)
                .roleType(roleType)
                .isPrimary(false)
                .grantedByUserId(grantedByUserId)
                .build());
        return buildResponse(targetUserId);
    }

    @Override
    @Transactional
    public UserRolesResponse revokeRole(Long targetUserId, UserTypeEnum roleType) throws GenericException {
        User target = userDao.findById(targetUserId, true);
        if (target == null) {
            throw new GenericException(ExceptionCodeEnum.USER_NOT_FOUND, "User not found");
        }
        if (ADMIN_TIER.contains(roleType)) {
            requireSuperAdmin();
        }
        UserRoleMapping mapping = userRoleMappingDao.find(targetUserId, roleType)
                .orElseThrow(() -> new GenericException(ExceptionCodeEnum.USER_ROLE_NOT_ASSIGNED,
                        "User does not hold the " + roleType + " role"));

        long roleCount = userRoleMappingDao.countByUserId(targetUserId);
        if (roleCount <= 1) {
            throw new GenericException(ExceptionCodeEnum.LAST_USER_ROLE_CANNOT_BE_REVOKED,
                    "Cannot revoke a user's last remaining role");
        }
        if (mapping.isPrimary()) {
            throw new GenericException(ExceptionCodeEnum.PRIMARY_USER_ROLE_CANNOT_BE_REVOKED,
                    "Cannot revoke the user's current primary/default role — switch their default role to another held role first");
        }
        userRoleMappingDao.delete(mapping);
        return buildResponse(targetUserId);
    }

    private UserRolesResponse buildResponse(Long userId) throws GenericException {
        User user = userDao.findById(userId, true);
        if (user == null) {
            throw new GenericException(ExceptionCodeEnum.USER_NOT_FOUND, "User not found");
        }
        List<UserRoleMapping> mappings = userRoleMappingDao.findByUserId(userId);
        List<UserTypeEnum> roles = mappings.stream().map(UserRoleMapping::getRoleType).distinct().toList();
        UserTypeEnum primary = mappings.stream()
                .filter(UserRoleMapping::isPrimary)
                .map(UserRoleMapping::getRoleType)
                .findFirst()
                .orElse(user.getUserType());
        return UserRolesResponse.builder()
                .userId(userId)
                .roles(roles)
                .primaryRole(primary)
                .build();
    }

    private void requireSuperAdmin() throws GenericException {
        User caller = helper.getAuthenticatedUser();
        if (caller.getUserType() != UserTypeEnum.SUPER_ADMIN) {
            throw new GenericException(ExceptionCodeEnum.FORBIDDEN,
                    "Only a super-admin can grant or revoke ADMIN/SUPER_ADMIN roles");
        }
    }
}
