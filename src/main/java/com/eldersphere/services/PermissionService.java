package com.eldersphere.services;

import com.eldersphere.dtos.Permission.PermissionRequestDTO;
import com.eldersphere.dtos.Permission.PermissionResponseDTO;
import com.eldersphere.dtos.Role.RolePermissionRequestDTO;
import com.eldersphere.dtos.Role.RolePermissionResponseDTO;
import com.eldersphere.exceptions.GenericException;

import java.util.List;

public interface PermissionService {
    PermissionResponseDTO create(PermissionRequestDTO request) throws GenericException;
    List<PermissionResponseDTO> getAll();
    void delete(Long id) throws GenericException;

    RolePermissionResponseDTO assignToRole(RolePermissionRequestDTO request) throws GenericException;
    void revokeFromRole(Long roleId, Long permissionId) throws GenericException;
    RolePermissionResponseDTO getByRole(Long roleId) throws GenericException;
}
