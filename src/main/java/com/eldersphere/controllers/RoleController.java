package com.eldersphere.controllers;

import com.eldersphere.dtos.Role.RoleRequestDTO;
import com.eldersphere.dtos.Role.RoleResponseDTO;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.RoleService;
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
@RequestMapping("/api/v1/roles")
@Tag(name = "Roles", description = "Super-admin RBAC role management")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Create role")
    @PostMapping
    public ResponseEntity<ResponseModel<RoleResponseDTO>> create(@Valid @RequestBody RoleRequestDTO request) throws GenericException {
        return ApiResponse.createSuccess(roleService.create(request), "Role created successfully");
    }

    @Operation(summary = "Update role")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseModel<RoleResponseDTO>> update(@PathVariable Long id, @Valid @RequestBody RoleRequestDTO request) throws GenericException {
        return ApiResponse.successResponse(roleService.update(id, request), "Role updated successfully");
    }

    @Operation(summary = "Get role by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<RoleResponseDTO>> getById(@PathVariable Long id) throws GenericException {
        return ApiResponse.successResponse(roleService.getById(id), "Role fetched successfully");
    }

    @Operation(summary = "List all roles")
    @GetMapping
    public ResponseEntity<ResponseModel<List<RoleResponseDTO>>> getAll() {
        return ApiResponse.successResponse(roleService.getAll(), "Roles fetched successfully");
    }

    @Operation(summary = "Delete role")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseModel<String>> delete(@PathVariable Long id) throws GenericException {
        roleService.delete(id);
        return ApiResponse.successResponse("Role deleted successfully");
    }
}
