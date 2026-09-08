package com.eldersphere.controllers;

import com.eldersphere.dtos.Notification.NotificationDeliveryLogResponse;
import com.eldersphere.dtos.Notification.NotificationResponseDTO;
import java.util.List;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.NotificationService;
import com.eldersphere.utils.Helper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "In-app notifications for the logged-in user")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final Helper helper;

    @Operation(summary = "List my notifications")
    @GetMapping
    public ResponseEntity<ResponseModel<Page<NotificationResponseDTO>>> getMine(
            @RequestHeader(value = "Authorization", required = false) String auth, Pageable pageable) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(notificationService.getByUser(userId, pageable), "Notifications fetched successfully");
    }

    @Operation(summary = "Get unread notification count")
    @GetMapping("/unread-count")
    public ResponseEntity<ResponseModel<Long>> getUnreadCount(
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(notificationService.getUnreadCount(userId), "Unread count fetched successfully");
    }

    @Operation(summary = "Mark a notification as read")
    @PutMapping("/{id}/read")
    public ResponseEntity<ResponseModel<String>> markAsRead(
            @RequestHeader(value = "Authorization", required = false) String auth, @PathVariable Long id) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        notificationService.markAsRead(id, userId);
        return ApiResponse.successResponse("Notification marked as read");
    }

    @Operation(summary = "Mark all notifications as read")
    @PutMapping("/read-all")
    public ResponseEntity<ResponseModel<String>> markAllAsRead(
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        notificationService.markAllAsRead(userId);
        return ApiResponse.successResponse("All notifications marked as read");
    }

    @Operation(summary = "Delete a notification")
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseModel<String>> delete(
            @RequestHeader(value = "Authorization", required = false) String auth, @PathVariable Long id) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        notificationService.delete(id, userId);
        return ApiResponse.successResponse("Notification deleted successfully");
    }

    @Operation(summary = "List simulated/in-app delivery log rows for a notification (admin)",
            description = "Shows which channels (IN_APP, SIMULATED_SMS, SIMULATED_EMAIL) a notification was dispatched over.")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/delivery-logs")
    public ResponseEntity<ResponseModel<List<NotificationDeliveryLogResponse>>> getDeliveryLogs(@PathVariable Long id) {
        return ApiResponse.successResponse(notificationService.getDeliveryLogs(id), "Delivery logs fetched successfully");
    }
}
