package com.eldersphere.controllers;

import com.eldersphere.dtos.Booking.BookingRequest;
import com.eldersphere.dtos.Booking.BookingResponse;
import com.eldersphere.dtos.Booking.BookingStatusUpdateRequest;
import com.eldersphere.enums.BookingStatusEnum;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.BookingService;
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

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/v1/bookings")
@Tag(name = "Bookings", description = "Family members book caretakers for their elders")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final Helper helper;

    @Operation(summary = "Create booking", description = "Set repeatWeekly=true with occurrences (2-12) to create a weekly recurring series; the response is the first occurrence and carries the shared recurringGroupId - fetch/cancel the full series via the /recurring endpoints below.")
    @PostMapping
    public ResponseEntity<ResponseModel<BookingResponse>> create(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @Valid @RequestBody BookingRequest request) throws GenericException {
        request.setFamilyUserId(helper.getUserIdFromHeader(auth));
        return ApiResponse.createSuccess(bookingService.create(request), "Booking created successfully");
    }

    @Operation(summary = "Get a recurring booking series", description = "All bookings sharing the given recurring_group_id.")
    @GetMapping("/recurring/{groupId}")
    public ResponseEntity<ResponseModel<List<BookingResponse>>> getRecurringSeries(@PathVariable String groupId) throws GenericException {
        return ApiResponse.successResponse(bookingService.getByRecurringGroupId(groupId), "Recurring booking series fetched successfully");
    }

    @Operation(summary = "Cancel a recurring booking series", description = "Cancels every booking sharing the given recurring_group_id. Restricted to the series' own family member or an ADMIN/SUPER_ADMIN.")
    @PutMapping("/recurring/{groupId}/cancel")
    public ResponseEntity<ResponseModel<List<BookingResponse>>> cancelRecurringSeries(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable String groupId) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(bookingService.cancelSeries(groupId, userId), "Recurring booking series cancelled successfully");
    }

    @Operation(summary = "Update booking status", description = "Transitions a booking through PENDING -> CONFIRMED -> IN_PROGRESS -> COMPLETED, or CANCELLED.")
    @PreAuthorize("isAuthenticated() and @adminPermissionGuard.has('BOOKINGS_MANAGE')")
    @PutMapping("/{id}/status")
    public ResponseEntity<ResponseModel<BookingResponse>> updateStatus(@PathVariable Long id, @Valid @RequestBody BookingStatusUpdateRequest request) throws GenericException {
        return ApiResponse.successResponse(bookingService.updateStatus(id, request.getStatus()), "Booking status updated successfully");
    }

    @Operation(summary = "Get booking by ID")
    @PreAuthorize("isAuthenticated() and @adminPermissionGuard.has('BOOKINGS_VIEW')")
    @GetMapping("/{id}")
    public ResponseEntity<ResponseModel<BookingResponse>> getById(@PathVariable Long id) throws GenericException {
        return ApiResponse.successResponse(bookingService.getById(id), "Booking fetched successfully");
    }

    @Operation(summary = "Search bookings", description = "Paginated booking search, filterable by family user, caretaker, and status.")
    @PreAuthorize("isAuthenticated() and @adminPermissionGuard.has('BOOKINGS_VIEW')")
    @GetMapping
    public ResponseEntity<ResponseModel<Page<BookingResponse>>> search(
            @RequestParam(required = false) Long familyUserId,
            @RequestParam(required = false) Long caretakerId,
            @RequestParam(required = false) BookingStatusEnum status,
            Pageable pageable) {
        return ApiResponse.successResponse(bookingService.search(familyUserId, caretakerId, status, pageable), "Bookings fetched successfully");
    }
}
