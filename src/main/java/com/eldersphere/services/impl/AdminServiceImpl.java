package com.eldersphere.services.impl;

import com.eldersphere.dao.role.RoleDao;
import com.eldersphere.dao.user.UserDao;
import com.eldersphere.dao.user.UserRoleMappingDao;
import com.eldersphere.dtos.Admin.AdminCreateUserRequest;
import com.eldersphere.dtos.User.UserResponse;
import com.eldersphere.entities.Role;
import com.eldersphere.entities.User;
import com.eldersphere.entities.UserRoleMapping;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.enums.UserStatusEnum;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.AdminService;
import com.eldersphere.services.UserRoleService;
import com.eldersphere.utils.Helper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    // "Admin-tier" accounts (ADMIN/SUPER_ADMIN) can only be created, edited or
    // deleted by a SUPER_ADMIN — a plain ADMIN cannot promote/demote/remove
    // other admin-tier accounts.
    private static final Set<UserTypeEnum> ADMIN_TIER = EnumSet.of(UserTypeEnum.ADMIN, UserTypeEnum.SUPER_ADMIN);

    private final UserDao userDao;
    private final RoleDao roleDao;
    private final UserRoleMappingDao userRoleMappingDao;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;
    private final Helper helper;

    @Override
    @Transactional
    public UserResponse createUser(AdminCreateUserRequest dto) throws GenericException {
        if (userDao.existsByEmail(dto.getEmail())) {
            throw new GenericException(ExceptionCodeEnum.DUPLICATE_EMAIL, "An account with this email already exists");
        }
        if (ADMIN_TIER.contains(dto.getUserType())) {
            requireSuperAdmin();
        }
        User user = User.builder()
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .fullName(dto.getFullName())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .userType(dto.getUserType())
                .status(UserStatusEnum.ACTIVE)
                .roleId(dto.getRoleId())
                .build();
        user = userDao.save(user);
        Long grantedByUserId = null;
        try {
            grantedByUserId = helper.getAuthenticatedUserId();
        } catch (GenericException ignored) {
            // Best-effort attribution only; the mapping row is still created without it.
        }
        userRoleService.ensurePrimaryMapping(user, grantedByUserId);
        return toResponse(user);
    }

    @Override
    public Page<UserResponse> listUsers(String search, UserTypeEnum userType, UserStatusEnum status, Pageable pageable) {
        return userDao.findByCriteria(search, userType, status, pageable).map(this::toResponse);
    }

    @Override
    public UserResponse getUser(Long id) throws GenericException {
        User user = userDao.findById(id, true);
        if (user == null) {
            throw new GenericException(ExceptionCodeEnum.USER_NOT_FOUND, "User not found");
        }
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateStatus(Long id, UserStatusEnum status) throws GenericException {
        User user = userDao.findById(id, true);
        if (user == null) {
            throw new GenericException(ExceptionCodeEnum.USER_NOT_FOUND, "User not found");
        }
        if (ADMIN_TIER.contains(user.getUserType())) {
            requireSuperAdmin();
        }
        user.setStatus(status);
        return toResponse(userDao.save(user));
    }

    @Override
    @Transactional
    public int bulkUpdateStatus(List<Long> userIds, UserStatusEnum status) throws GenericException {
        List<User> users = userIds.stream()
                .map(id -> userDao.findById(id, true))
                .filter(java.util.Objects::nonNull)
                .toList();
        boolean touchesAdminTier = users.stream().anyMatch(u -> ADMIN_TIER.contains(u.getUserType()));
        if (touchesAdminTier) {
            requireSuperAdmin();
        }
        int updated = 0;
        for (User user : users) {
            user.setStatus(status);
            userDao.save(user);
            updated++;
        }
        return updated;
    }

    @Override
    @Transactional
    public UserResponse assignRole(Long id, Long roleId) throws GenericException {
        User user = userDao.findById(id, true);
        if (user == null) {
            throw new GenericException(ExceptionCodeEnum.USER_NOT_FOUND, "User not found");
        }
        if (ADMIN_TIER.contains(user.getUserType())) {
            requireSuperAdmin();
        }
        if (roleId != null && roleDao.findById(roleId, true) == null) {
            throw new GenericException(ExceptionCodeEnum.ROLE_NOT_FOUND, "Role not found");
        }
        user.setRoleId(roleId);
        return toResponse(userDao.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(Long id) throws GenericException {
        User user = userDao.findById(id, true);
        if (user == null) {
            throw new GenericException(ExceptionCodeEnum.USER_NOT_FOUND, "User not found");
        }
        if (ADMIN_TIER.contains(user.getUserType())) {
            requireSuperAdmin();
        }
        userDao.deleteById(id);
    }

    private void requireSuperAdmin() throws GenericException {
        User caller = helper.getAuthenticatedUser();
        if (caller.getUserType() != UserTypeEnum.SUPER_ADMIN) {
            throw new GenericException(ExceptionCodeEnum.FORBIDDEN,
                    "Only a super-admin can create, edit or delete admin-tier accounts");
        }
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setFullName(user.getFullName());
        response.setUserType(user.getUserType());
        response.setStatus(user.getStatus());
        response.setRoleId(user.getRoleId());
        if (user.getRoleId() != null) {
            Role role = roleDao.findById(user.getRoleId(), true);
            if (role != null) response.setRoleName(role.getName());
        }
        response.setRoles(userRoleMappingDao.findByUserId(user.getId()).stream()
                .map(UserRoleMapping::getRoleType)
                .distinct()
                .toList());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}
