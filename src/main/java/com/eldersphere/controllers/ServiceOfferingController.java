package com.eldersphere.controllers;

import com.eldersphere.dtos.ServiceOffering.ServiceOfferingRequest;
import com.eldersphere.dtos.ServiceOffering.ServiceOfferingResponse;
import com.eldersphere.enums.ServiceCategoryEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.ServiceOfferingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/services")
@Tag(name = "Service Offerings", description = "Catalog of bookable services (nursing, physiotherapy, medication assistance, companion care)")
@RequiredArgsConstructor
public class ServiceOfferingController {

    private final ServiceOfferingService serviceOfferingService;

    @Operation(summary = "Create service offering (admin)")
    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('SERVICES_MANAGE')")
    @PostMapping
    public ResponseEntity<ResponseModel<ServiceOfferingResponse>> create(@Valid @RequestBody ServiceOfferingRequest request) throws GenericException {
        return ApiResponse.createSuccess(serviceOfferingService.create(request), "Service offering created successfully");
    }

    @Operation(summary = "Update service offering (admin)")
    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('SERVICES_MANAGE')")
    @PutMapping("/{id}")
    public ResponseEntity<ResponseModel<ServiceOfferingResponse>> update(@PathVariable Long id, @Valid @RequestBody ServiceOfferingRequest request) throws GenericException {
        return ApiResponse.successResponse(serviceOfferingService.update(id, request), "Service offering updated successfully");
    }

    @Operation(summary = "Get service offering by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<ServiceOfferingResponse>> getById(@PathVariable Long id) throws GenericException {
        return ApiResponse.successResponse(serviceOfferingService.getById(id), "Service offering fetched successfully");
    }

    @Operation(summary = "List service offerings", description = "Public catalog listing, optionally filtered by category.")
    @GetMapping
    public ResponseEntity<ResponseModel<Page<ServiceOfferingResponse>>> getAll(
            @RequestParam(required = false) ServiceCategoryEnum category, Pageable pageable) {
        return ApiResponse.successResponse(serviceOfferingService.getAll(category, pageable), "Service offerings fetched successfully");
    }

    @Operation(summary = "Delete service offering (admin)")
    @PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('SERVICES_MANAGE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseModel<String>> delete(@PathVariable Long id) throws GenericException {
        serviceOfferingService.delete(id);
        return ApiResponse.successResponse("Service offering deleted successfully");
    }
}
