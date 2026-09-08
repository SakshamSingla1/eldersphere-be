package com.eldersphere.controllers;

import com.eldersphere.dtos.Dashboard.CaretakerDashboardSummaryDTO;
import com.eldersphere.dtos.Dashboard.DashboardSummaryDTO;
import com.eldersphere.dtos.Dashboard.ElderDashboardSummaryDTO;
import com.eldersphere.dtos.Dashboard.FamilyDashboardSummaryDTO;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.DashboardService;
import com.eldersphere.utils.Helper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Per-role summary stats: admin platform overview, and family/caretaker/elder personal dashboards")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final Helper helper;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get admin dashboard summary", description = "Platform-wide stats: bookings, active caretakers, pending emergencies, revenue, recent activity.")
    @GetMapping("/summary")
    public ResponseEntity<ResponseModel<DashboardSummaryDTO>> getSummary() {
        return ApiResponse.successResponse(dashboardService.getSummary(), "Dashboard summary fetched successfully");
    }

    @PreAuthorize("hasRole('FAMILY_MEMBER')")
    @Operation(summary = "Get family dashboard summary", description = "Managed elder count, upcoming bookings, unread notifications, and recent activity for the logged-in family member.")
    @GetMapping("/family-summary")
    public ResponseEntity<ResponseModel<FamilyDashboardSummaryDTO>> getFamilySummary(
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        Long familyUserId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(dashboardService.getFamilySummary(familyUserId), "Family dashboard summary fetched successfully");
    }

    @PreAuthorize("hasRole('CARETAKER')")
    @Operation(summary = "Get caretaker dashboard summary", description = "Upcoming/completed booking counts, average rating, unread notifications, and recent activity for the logged-in caretaker.")
    @GetMapping("/caretaker-summary")
    public ResponseEntity<ResponseModel<CaretakerDashboardSummaryDTO>> getCaretakerSummary(
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        Long caretakerUserId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(dashboardService.getCaretakerSummary(caretakerUserId), "Caretaker dashboard summary fetched successfully");
    }

    @PreAuthorize("hasRole('ELDER')")
    @Operation(summary = "Get elder dashboard summary", description = "Upcoming bookings, active emergency alerts, unread notifications, and recently shared medical records for the logged-in elder.")
    @GetMapping("/elder-summary")
    public ResponseEntity<ResponseModel<ElderDashboardSummaryDTO>> getElderSummary(
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        Long elderUserId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(dashboardService.getElderSummary(elderUserId), "Elder dashboard summary fetched successfully");
    }
}
