package com.eldersphere.controllers;

import com.eldersphere.dtos.Emergency.EmergencyAlertRequest;
import com.eldersphere.dtos.Emergency.EmergencyAlertResponse;
import com.eldersphere.dtos.Emergency.EmergencyAlertUpdateRequest;
import com.eldersphere.enums.EmergencyAlertStatusEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.EmergencyAlertService;
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

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/v1/emergency-alerts")
@Tag(name = "Emergency Alerts", description = "Panic-button style emergency alerts for elders, with acknowledgement/resolution tracking")
@RequiredArgsConstructor
public class EmergencyAlertController {

    private final EmergencyAlertService emergencyAlertService;
    private final Helper helper;

    @Operation(summary = "Trigger emergency alert")
    @PostMapping
    public ResponseEntity<ResponseModel<EmergencyAlertResponse>> trigger(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @Valid @RequestBody EmergencyAlertRequest request) throws GenericException {
        Long triggeredByUserId = helper.getUserIdFromHeader(auth);
        return ApiResponse.createSuccess(emergencyAlertService.trigger(triggeredByUserId, request), "Emergency alert triggered");
    }

    @Operation(summary = "Acknowledge or resolve an emergency alert")
    @PreAuthorize("isAuthenticated() and @adminPermissionGuard.has('EMERGENCY_ALERTS_MANAGE')")
    @PutMapping("/{id}/status")
    public ResponseEntity<ResponseModel<EmergencyAlertResponse>> updateStatus(@PathVariable Long id, @Valid @RequestBody EmergencyAlertUpdateRequest request) throws GenericException {
        return ApiResponse.successResponse(emergencyAlertService.updateStatus(id, request), "Emergency alert updated successfully");
    }

    @Operation(summary = "Get emergency alert by ID")
    @PreAuthorize("isAuthenticated() and @adminPermissionGuard.has('EMERGENCY_ALERTS_VIEW')")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<EmergencyAlertResponse>> getById(@PathVariable Long id) throws GenericException {
        return ApiResponse.successResponse(emergencyAlertService.getById(id), "Emergency alert fetched successfully");
    }

    @Operation(summary = "Search emergency alerts", description = "Filter by elder profile or status (e.g. list all pending TRIGGERED/ACKNOWLEDGED alerts for ops dashboards).")
    @PreAuthorize("isAuthenticated() and @adminPermissionGuard.has('EMERGENCY_ALERTS_VIEW')")
    @GetMapping
    public ResponseEntity<ResponseModel<Page<EmergencyAlertResponse>>> search(
            @RequestParam(required = false) Long elderProfileId,
            @RequestParam(required = false) EmergencyAlertStatusEnum status,
            Pageable pageable) {
        return ApiResponse.successResponse(emergencyAlertService.search(elderProfileId, status, pageable), "Emergency alerts fetched successfully");
    }
}
