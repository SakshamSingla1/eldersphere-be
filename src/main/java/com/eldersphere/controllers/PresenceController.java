package com.eldersphere.controllers;

import com.eldersphere.presence.PresenceTracker;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/v1/presence")
@Tag(name = "Presence", description = "Online/offline status for users, kept live over /topic/presence")
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceTracker presenceTracker;

    @Operation(summary = "Get online status for a set of users", description = "Initial-paint snapshot for a client that has just connected and hasn't received any /topic/presence events yet.")
    @GetMapping("/status")
    public ResponseEntity<ResponseModel<Map<Long, Boolean>>> getStatus(@RequestParam List<Long> userIds) {
        Map<Long, Boolean> status = new LinkedHashMap<>();
        userIds.forEach(userId -> status.put(userId, presenceTracker.isOnline(userId)));
        return ApiResponse.successResponse(status, "Presence status fetched successfully");
    }
}
