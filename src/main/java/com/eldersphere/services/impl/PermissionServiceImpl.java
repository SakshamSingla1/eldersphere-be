package com.eldersphere.services.impl;

import com.eldersphere.dao.permission.PermissionDao;
import com.eldersphere.dao.role.RoleDao;
import com.eldersphere.dao.role.RolePermissionDao;
import com.eldersphere.dtos.Permission.PermissionRequestDTO;
import com.eldersphere.dtos.Permission.PermissionResponseDTO;
import com.eldersphere.dtos.Role.RolePermissionRequestDTO;
import com.eldersphere.dtos.Role.RolePermissionResponseDTO;
import com.eldersphere.entities.Permission;
import com.eldersphere.entities.Role;
import com.eldersphere.entities.RolePermission;
import com.eldersphere.enums.ExceptionCodeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.services.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionDao permissionDao;
    private final RoleDao roleDao;
    private final RolePermissionDao rolePermissionDao;

    @Override
    @Transactional
    public PermissionResponseDTO create(PermissionRequestDTO request) throws GenericException {
        if (permissionDao.existsByName(request.getName())) {
            throw new GenericException(ExceptionCodeEnum.DUPLICATE_PERMISSION, "A permission with this name already exists");
        }
        Permission permission = Permission.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return toResponse(permissionDao.save(permission));
    }

    @Override
    public List<PermissionResponseDTO> getAll() {
        return permissionDao.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) throws GenericException {
        Permission permission = permissionDao.findById(id, true);
        if (permission == null) {
            throw new GenericException(ExceptionCodeEnum.PERMISSION_NOT_FOUND, "Permission not found");
        }
        permissionDao.deleteById(id);
    }

    @Override
    @Transactional
    public RolePermissionResponseDTO assignToRole(RolePermissionRequestDTO request) throws GenericException {
        Role role = roleDao.findById(request.getRoleId(), true);
        if (role == null) {
            throw new GenericException(ExceptionCodeEnum.ROLE_NOT_FOUND, "Role not found");
        }
        Permission permission = permissionDao.findById(request.getPermissionId(), true);
        if (permission == null) {
            throw new GenericException(ExceptionCodeEnum.PERMISSION_NOT_FOUND, "Permission not found");
        }
        if (rolePermissionDao.existsByRoleIdAndPermissionId(request.getRoleId(), request.getPermissionId())) {
            throw new GenericException(ExceptionCodeEnum.DUPLICATE_ROLE_PERMISSION, "This permission is already assigned to the role");
        }
        rolePermissionDao.save(RolePermission.builder()
                .roleId(request.getRoleId())
                .permissionId(request.getPermissionId())
                .build());
        return getByRole(request.getRoleId());
    }

    @Override
    @Transactional
    public void revokeFromRole(Long roleId, Long permissionId) throws GenericException {
        Role role = roleDao.findById(roleId, true);
        if (role == null) {
            throw new GenericException(ExceptionCodeEnum.ROLE_NOT_FOUND, "Role not found");
        }
        rolePermissionDao.deleteByRoleIdAndPermissionId(roleId, permissionId);
    }

    @Override
    public RolePermissionResponseDTO getByRole(Long roleId) throws GenericException {
        Role role = roleDao.findById(roleId, true);
        if (role == null) {
            throw new GenericException(ExceptionCodeEnum.ROLE_NOT_FOUND, "Role not found");
        }
        List<RolePermission> mappings = rolePermissionDao.findByRoleId(roleId);
        List<RolePermissionResponseDTO.PermissionResponseDTOLite> permissions = mappings.stream()
                .map(m -> {
                    Permission p = permissionDao.findById(m.getPermissionId(), true);
                    return RolePermissionResponseDTO.PermissionResponseDTOLite.builder()
                            .id(m.getPermissionId())
                            .name(p != null ? p.getName() : null)
                            .build();
                })
                .collect(Collectors.toList());
        return RolePermissionResponseDTO.builder()
                .roleId(role.getId())
                .roleName(role.getName())
                .permissions(permissions)
                .build();
    }

    private PermissionResponseDTO toResponse(Permission permission) {
        PermissionResponseDTO dto = new PermissionResponseDTO();
        dto.setId(permission.getId());
        dto.setName(permission.getName());
        dto.setDescription(permission.getDescription());
        dto.setCreatedAt(permission.getCreatedAt());
        dto.setUpdatedAt(permission.getUpdatedAt());
        return dto;
    }
}
