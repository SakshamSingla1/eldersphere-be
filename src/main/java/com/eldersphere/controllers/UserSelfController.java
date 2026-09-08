package com.eldersphere.controllers;

import com.eldersphere.dtos.ColorTheme.UserThemeResponseDTO;
import com.eldersphere.dtos.ColorTheme.UserThemeUpdateRequest;
import com.eldersphere.dtos.Notification.NotificationPreferenceDTO;
import com.eldersphere.dtos.Notification.NotificationPreferencesUpdateRequest;
import com.eldersphere.dtos.Notification.PushSubscriptionRequest;
import com.eldersphere.dtos.Notification.PushUnsubscribeRequest;
import com.eldersphere.dtos.User.DefaultRoleUpdateRequest;
import com.eldersphere.dtos.User.UserDataExportResponse;
import com.eldersphere.dtos.User.UserRolesResponse;
import com.eldersphere.exceptions.GenericException;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.services.NotificationPreferenceService;
import com.eldersphere.services.PushSubscriptionService;
import com.eldersphere.services.UserDataExportService;
import com.eldersphere.services.UserRoleService;
import com.eldersphere.services.UserThemeService;
import com.eldersphere.utils.Helper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/v1/users/me")
@Tag(name = "Users - Self", description = "The logged-in user's own multi-role info, notification preferences, and data export")
@RequiredArgsConstructor
public class UserSelfController {

    private final UserRoleService userRoleService;
    private final NotificationPreferenceService notificationPreferenceService;
    private final PushSubscriptionService pushSubscriptionService;
    private final UserDataExportService userDataExportService;
    private final UserThemeService userThemeService;
    private final Helper helper;

    @Operation(summary = "Get my roles", description = "Returns every role the caller holds and which one is currently their primary/default role.")
    @GetMapping("/roles")
    public ResponseEntity<ResponseModel<UserRolesResponse>> getMyRoles(
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(userRoleService.getMyRoles(userId), "Roles fetched successfully");
    }

    @Operation(summary = "Switch my default role", description = "Changes which of the caller's already-held roles is their primary/default role (the role they land on after login). Rejects switching to a role they don't hold.")
    @PutMapping("/default-role")
    public ResponseEntity<ResponseModel<UserRolesResponse>> switchDefaultRole(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @Valid @RequestBody DefaultRoleUpdateRequest request) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(userRoleService.switchDefaultRole(userId, request.getRoleType()), "Default role updated successfully");
    }

    @Operation(summary = "Get my notification preferences", description = "One entry per notification type; a type the caller has never customized comes back with the default (in-app on, email/SMS off).")
    @GetMapping("/notification-preferences")
    public ResponseEntity<ResponseModel<List<NotificationPreferenceDTO>>> getMyNotificationPreferences(
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(notificationPreferenceService.getMine(userId), "Notification preferences fetched successfully");
    }

    @Operation(summary = "Update my notification preferences", description = "Bulk upsert - only the notification types included in the request body are changed. Returns the full, freshly-resolved list for every notification type.")
    @PutMapping("/notification-preferences")
    public ResponseEntity<ResponseModel<List<NotificationPreferenceDTO>>> updateMyNotificationPreferences(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @Valid @RequestBody NotificationPreferencesUpdateRequest request) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(notificationPreferenceService.updateMine(userId, request.getPreferences()), "Notification preferences updated successfully");
    }

    @Operation(summary = "Register a Web Push subscription", description = "Idempotent on endpoint - registers (or re-registers) a browser's push subscription so this account can receive real Web Push notifications on it, subject to that notification type's WEB_PUSH preference.")
    @PostMapping("/push-subscriptions")
    public ResponseEntity<ResponseModel<Void>> registerPushSubscription(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @Valid @RequestBody PushSubscriptionRequest request) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        pushSubscriptionService.register(userId, request);
        return ApiResponse.successResponse(null, "Push subscription registered successfully");
    }

    @Operation(summary = "Remove a Web Push subscription", description = "Unsubscribes the given endpoint from this account. A no-op if it wasn't registered (e.g. already cleaned up as stale).")
    @DeleteMapping("/push-subscriptions")
    public ResponseEntity<ResponseModel<Void>> removePushSubscription(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @Valid @RequestBody PushUnsubscribeRequest request) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        pushSubscriptionService.unsubscribe(userId, request.getEndpoint());
        return ApiResponse.successResponse(null, "Push subscription removed successfully");
    }

    @Operation(summary = "Export my data", description = "Download-my-data: a JSON dump of the caller's own profile, bookings, reviews, medical records, and notifications.")
    @GetMapping("/export")
    public ResponseEntity<ResponseModel<UserDataExportResponse>> exportMyData(
            @RequestHeader(value = "Authorization", required = false) String auth) throws GenericException {
        Long userId = helper.getUserIdFromHeader(auth);
        return ApiResponse.successResponse(userDataExportService.export(userId), "Data export generated successfully");
    }
}
