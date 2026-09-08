package com.eldersphere.controllers;

import com.eldersphere.dtos.Analytics.BookingTimeseriesPointDTO;
import com.eldersphere.dtos.Analytics.CaretakerLeaderboardEntryDTO;
import com.eldersphere.dtos.Analytics.RevenueTimeseriesPointDTO;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.AnalyticsService;
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
}
