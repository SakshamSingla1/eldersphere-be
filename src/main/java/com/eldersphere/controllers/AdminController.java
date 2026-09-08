package com.eldersphere.controllers;

import com.eldersphere.dtos.Admin.*;
import com.eldersphere.dtos.User.UserResponse;
import com.eldersphere.dtos.User.UserRolesResponse;
import com.eldersphere.enums.UserStatusEnum;
import com.eldersphere.enums.UserTypeEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.AdminService;
import com.eldersphere.services.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Admin - Users", description = "Admin user management: list/filter, activate/deactivate, assign roles, bulk actions")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserRoleService userRoleService;

    @Operation(summary = "Create a user (admin)", description = "Creates a fully-active user account directly, bypassing self-registration.")
    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('USERS_MANAGE')")
    @PostMapping
    public ResponseEntity<ResponseModel<UserResponse>> createUser(@Valid @RequestBody AdminCreateUserRequest request) throws GenericException {
        UserResponse response = adminService.createUser(request);
        return ApiResponse.createSuccess(response, "User created successfully");
    }

    @Operation(summary = "List/filter users", description = "Paginated list of users, filterable by search text, user type, and status.")
    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('USERS_VIEW')")
    @GetMapping
    public ResponseEntity<ResponseModel<Page<UserResponse>>> listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserTypeEnum userType,
            @RequestParam(required = false) UserStatusEnum status,
            Pageable pageable) {
        Page<UserResponse> response = adminService.listUsers(search, userType, status, pageable);
        return ApiResponse.successResponse(response, "Users fetched successfully");
    }

    @Operation(summary = "Get user by ID")
    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('USERS_VIEW')")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<UserResponse>> getUser(@PathVariable Long id) throws GenericException {
        return ApiResponse.successResponse(adminService.getUser(id), "User fetched successfully");
    }

    @Operation(summary = "Activate/deactivate a user")
    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('USERS_MANAGE')")
    @PutMapping("/{id}/status")
    public ResponseEntity<ResponseModel<UserResponse>> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) throws GenericException {
        UserResponse response = adminService.updateStatus(id, request.getStatus());
        return ApiResponse.successResponse(response, "User status updated successfully");
    }

    @Operation(summary = "Bulk activate/deactivate users")
    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('USERS_MANAGE')")
    @PutMapping("/bulk-status")
    public ResponseEntity<ResponseModel<Integer>> bulkUpdateStatus(@Valid @RequestBody BulkStatusUpdateRequest request) throws GenericException {
        int updated = adminService.bulkUpdateStatus(request.getUserIds(), request.getStatus());
        return ApiResponse.successResponse(updated, updated + " users updated successfully");
    }

    @Operation(summary = "Assign a role to a user (admin RBAC)")
    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('USERS_MANAGE')")
    @PutMapping("/{id}/role")
    public ResponseEntity<ResponseModel<UserResponse>> assignRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) throws GenericException {
        UserResponse response = adminService.assignRole(id, request.getRoleId());
        return ApiResponse.successResponse(response, "Role assigned successfully");
    }

    @Operation(summary = "Delete a user")
    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('USERS_MANAGE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseModel<String>> deleteUser(@PathVariable Long id) throws GenericException {
        adminService.deleteUser(id);
        return ApiResponse.successResponse("User deleted successfully");
    }

    @Operation(summary = "Grant a role to a user (multi-role)",
            description = "Adds an additional UserTypeEnum role to a user's held roles, on top of whatever they already have. " +
                    "Granting ADMIN or SUPER_ADMIN requires the caller to be SUPER_ADMIN.")
    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('USERS_MANAGE')")
    @PostMapping("/{id}/roles")
    public ResponseEntity<ResponseModel<UserRolesResponse>> grantRole(@PathVariable Long id, @Valid @RequestBody GrantUserRoleRequest request) throws GenericException {
        return ApiResponse.successResponse(userRoleService.grantRole(id, request.getRoleType()), "Role granted successfully");
    }

    @Operation(summary = "Revoke a role from a user (multi-role)",
            description = "Removes a UserTypeEnum role from a user's held roles. Rejects revoking their last remaining role, " +
                    "and rejects revoking their current primary/default role (switch their default role first). " +
                    "Revoking ADMIN or SUPER_ADMIN requires the caller to be SUPER_ADMIN.")
    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('USERS_MANAGE')")
    @DeleteMapping("/{id}/roles/{roleType}")
    public ResponseEntity<ResponseModel<UserRolesResponse>> revokeRole(@PathVariable Long id, @PathVariable UserTypeEnum roleType) throws GenericException {
        return ApiResponse.successResponse(userRoleService.revokeRole(id, roleType), "Role revoked successfully");
    }
}
