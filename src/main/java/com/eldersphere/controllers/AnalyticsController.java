package com.eldersphere.controllers;

import com.eldersphere.dtos.Analytics.BookingTimeseriesPointDTO;
import com.eldersphere.dtos.Analytics.CaretakerBookingsRatingTrendDTO;
import com.eldersphere.dtos.Analytics.CaretakerLeaderboardEntryDTO;
import com.eldersphere.dtos.Analytics.FamilySpendingSummaryDTO;
import com.eldersphere.dtos.Analytics.RevenueTimeseriesPointDTO;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.AnalyticsService;
import com.eldersphere.utils.Helper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@PreAuthorize("hasRole('ADMIN') and @adminPermissionGuard.has('ANALYTICS_VIEW')")
@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "Admin/Super Admin dashboards: booking and revenue time-series, caretaker leaderboard - all computed from real data")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final Helper helper;

    @Operation(summary = "Bookings time-series", description = "Count of bookings per day/week (broken down by status) over a date range, based on scheduled_date.")
    @GetMapping("/bookings-timeseries")
    public ResponseEntity<ResponseModel<List<BookingTimeseriesPointDTO>>> getBookingTimeseries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "day") String granularity) throws GenericException {
        return ApiResponse.successResponse(analyticsService.getBookingTimeseries(start, end, granularity), "Booking timeseries fetched successfully");
    }

    @Operation(summary = "Revenue time-series", description = "Sum of cost for COMPLETED bookings per day/week over a date range, based on scheduled_date.")
    @GetMapping("/revenue-timeseries")
    public ResponseEntity<ResponseModel<List<RevenueTimeseriesPointDTO>>> getRevenueTimeseries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "day") String granularity) throws GenericException {
        return ApiResponse.successResponse(analyticsService.getRevenueTimeseries(start, end, granularity), "Revenue timeseries fetched successfully");
    }

    @Operation(summary = "Caretaker leaderboard", description = "Top N caretakers by average rating or completed-booking count.")
    @GetMapping("/caretaker-leaderboard")
    public ResponseEntity<ResponseModel<List<CaretakerLeaderboardEntryDTO>>> getCaretakerLeaderboard(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "RATING") String sortBy) throws GenericException {
        return ApiResponse.successResponse(analyticsService.getCaretakerLeaderboard(limit, sortBy), "Caretaker leaderboard fetched successfully");
    }

    @Operation(summary = "My spending over time (family)", description = "Self-scoped: monthly total of SUCCEEDED payments for the calling family member's own bookings, all-time, plus a current bookings-by-status breakdown.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me/family-spending")
    public ResponseEntity<ResponseModel<FamilySpendingSummaryDTO>> getMyFamilySpending(
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        Long familyUserId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(analyticsService.getMyFamilySpending(familyUserId), "Family spending fetched successfully");
    }

    @Operation(summary = "My earnings over time (caretaker)", description = "Self-scoped: monthly total of SUCCEEDED payments for the calling caretaker's own bookings, all-time. Resolves the caretaker profile from the JWT the same way GET /payments/me/earnings does.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me/caretaker-earnings")
    public ResponseEntity<ResponseModel<List<RevenueTimeseriesPointDTO>>> getMyCaretakerEarnings(
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(analyticsService.getMyCaretakerEarnings(userId), "Caretaker earnings fetched successfully");
    }

    @Operation(summary = "My bookings-per-week and rating trend (caretaker)", description = "Self-scoped: weekly count of the calling caretaker's own COMPLETED bookings, all-time, plus their average review rating per month.")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me/caretaker-bookings-rating-trend")
    public ResponseEntity<ResponseModel<CaretakerBookingsRatingTrendDTO>> getMyCaretakerBookingsRatingTrend(
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(analyticsService.getMyCaretakerBookingsRatingTrend(userId), "Caretaker bookings and rating trend fetched successfully");
    }
}
