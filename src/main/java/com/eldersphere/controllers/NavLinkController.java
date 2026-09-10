package com.eldersphere.controllers;

import com.eldersphere.dtos.NavLink.NavLinkRequest;
import com.eldersphere.dtos.NavLink.NavLinkResponse;
import com.eldersphere.entities.User;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.NavLinkService;
import com.eldersphere.utils.Helper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/nav-links")
@Tag(name = "Navigation Links", description = "DB-backed sidebar navigation items per portal (user type) - replaces what used to be a hardcoded array per frontend route file")
@RequiredArgsConstructor
public class NavLinkController {

    private final NavLinkService navLinkService;
    private final Helper helper;

    @Operation(summary = "My sidebar", description = "Resolves the caller's own portal's nav items - active items for their user type, with super-admin-only and required-permission gates already applied, in display order.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<ResponseModel<List<NavLinkResponse>>> getMine(
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        User caller = helper.getUserFromHeader(auth);
        return ApiResponse.successResponse(navLinkService.getMine(caller), "Navigation links fetched successfully");
    }

    @Operation(summary = "Create navigation link (super admin)")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<ResponseModel<NavLinkResponse>> create(@Valid @RequestBody NavLinkRequest request) throws GenericException {
        return ApiResponse.createSuccess(navLinkService.create(request), "Navigation link created successfully");
    }

    @Operation(summary = "Update navigation link (super admin)")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseModel<NavLinkResponse>> update(@PathVariable Long id, @Valid @RequestBody NavLinkRequest request) throws GenericException {
        return ApiResponse.successResponse(navLinkService.update(id, request), "Navigation link updated successfully");
    }

    @Operation(summary = "Get navigation link by ID (super admin)")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<NavLinkResponse>> getById(@PathVariable Long id) throws GenericException {
        return ApiResponse.successResponse(navLinkService.getById(id), "Navigation link fetched successfully");
    }

    @Operation(summary = "List navigation links (super admin)", description = "Every nav link across every portal, for the management listing.")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ResponseModel<Page<NavLinkResponse>>> getAll(Pageable pageable) {
        return ApiResponse.successResponse(navLinkService.getAll(pageable), "Navigation links fetched successfully");
    }

    @Operation(summary = "Delete navigation link (super admin)")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseModel<String>> delete(@PathVariable Long id) throws GenericException {
        navLinkService.delete(id);
        return ApiResponse.successResponse("Navigation link deleted successfully");
    }
}
