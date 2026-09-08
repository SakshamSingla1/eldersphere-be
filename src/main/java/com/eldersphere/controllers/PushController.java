package com.eldersphere.controllers;

import com.eldersphere.dtos.Notification.VapidPublicKeyResponse;
import com.eldersphere.payload.ApiResponse;
import com.eldersphere.payload.ResponseModel;
import com.eldersphere.webpush.VapidKeyProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/push")
@Tag(name = "Web Push", description = "Public Web Push configuration - the VAPID public key the frontend needs to subscribe")
@RequiredArgsConstructor
public class PushController {

    private final VapidKeyProvider vapidKeyProvider;

    @Operation(summary = "Get the server's VAPID public key", description = "Public, no auth required - the frontend passes this straight to pushManager.subscribe({ applicationServerKey }).")
    @GetMapping("/vapid-public-key")
    public ResponseEntity<ResponseModel<VapidPublicKeyResponse>> getVapidPublicKey() {
        return ApiResponse.successResponse(
                VapidPublicKeyResponse.builder().publicKey(vapidKeyProvider.getPublicKey()).build(),
                "VAPID public key fetched successfully");
    }
}
