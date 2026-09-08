package com.eldersphere.services;

import com.eldersphere.dtos.User.UserRolesResponse;
import com.eldersphere.entities.User;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.exceptions.GenericException;

import java.util.List;

public interface UserRoleService {

    /**
     * All role types currently held by a user, in no particular guaranteed
     * order. Used when issuing/refreshing a JWT and when building Spring
     * Security authorities. Never empty for a user that has been through
     * {@link #ensurePrimaryMapping}.
     */
    List<UserTypeEnum> getRoleTypes(Long userId);

    /**
     * Creates the initial (primary) role mapping row for a newly created
     * user, matching their {@code userType}. Called once at user creation
     * time (self-registration or admin-created) so every user always has
     * at least one row in user_role_mappings.
     */
    void ensurePrimaryMapping(User user, Long grantedByUserId);

    UserRolesResponse getMyRoles(Long userId) throws GenericException;

    /**
     * Switches which of the caller's own already-held roles is their
     * primary/default role. Rejects switching to a role they don't hold.
     */
    UserRolesResponse switchDefaultRole(Long userId, UserTypeEnum newPrimaryRole) throws GenericException;

    /**
     * Grants an additional role to a target user (admin action). Granting
     * ADMIN/SUPER_ADMIN requires the caller to be SUPER_ADMIN.
     */
    UserRolesResponse grantRole(Long targetUserId, UserTypeEnum roleType) throws GenericException;

    /**
     * Revokes a role from a target user (admin action). Rejects revoking a
     * user's last remaining role, and rejects revoking their current
     * primary role (a new primary must be set first via switchDefaultRole).
     * Revoking ADMIN/SUPER_ADMIN requires the caller to be SUPER_ADMIN.
     */
    UserRolesResponse revokeRole(Long targetUserId, UserTypeEnum roleType) throws GenericException;
}
