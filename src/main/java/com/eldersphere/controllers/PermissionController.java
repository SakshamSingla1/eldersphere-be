package com.eldersphere.controllers;

import com.eldersphere.dtos.Permission.PermissionRequestDTO;
import com.eldersphere.dtos.Permission.PermissionResponseDTO;
import com.eldersphere.dtos.Role.RolePermissionRequestDTO;
import com.eldersphere.dtos.Role.RolePermissionResponseDTO;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("hasRole('SUPER_ADMIN')")
@RestController
@RequestMapping("/api/v1/permissions")
@Tag(name = "Permissions", description = "Super-admin RBAC permission management and role-permission assignment")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "Create permission")
    @PostMapping
    public ResponseEntity<ResponseModel<PermissionResponseDTO>> create(@Valid @RequestBody PermissionRequestDTO request) throws GenericException {
        return ApiResponse.createSuccess(permissionService.create(request), "Permission created successfully");
    }

    @Operation(summary = "List all permissions")
    @GetMapping
    public ResponseEntity<ResponseModel<List<PermissionResponseDTO>>> getAll() {
        return ApiResponse.successResponse(permissionService.getAll(), "Permissions fetched successfully");
    }

    @Operation(summary = "Delete permission")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseModel<String>> delete(@PathVariable Long id) throws GenericException {
        permissionService.delete(id);
        return ApiResponse.successResponse("Permission deleted successfully");
    }

    @Operation(summary = "Assign a permission to a role")
    @PostMapping("/assign")
    public ResponseEntity<ResponseModel<RolePermissionResponseDTO>> assign(@Valid @RequestBody RolePermissionRequestDTO request) throws GenericException {
        return ApiResponse.successResponse(permissionService.assignToRole(request), "Permission assigned to role successfully");
    }

    @Operation(summary = "Revoke a permission from a role")
    @DeleteMapping("/roles/{roleId}/{permissionId}")
    public ResponseEntity<ResponseModel<String>> revoke(@PathVariable Long roleId, @PathVariable Long permissionId) throws GenericException {
        permissionService.revokeFromRole(roleId, permissionId);
        return ApiResponse.successResponse("Permission revoked from role successfully");
    }

    @Operation(summary = "Get permissions assigned to a role")
    @GetMapping("/roles/{roleId}")
    public ResponseEntity<ResponseModel<RolePermissionResponseDTO>> getByRole(@PathVariable Long roleId) throws GenericException {
        return ApiResponse.successResponse(permissionService.getByRole(roleId), "Role permissions fetched successfully");
    }
}
