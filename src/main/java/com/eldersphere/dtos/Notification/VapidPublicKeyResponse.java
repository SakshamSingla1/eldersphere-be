package com.eldersphere.dtos.Notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body for the public {@code GET /api/v1/push/vapid-public-key} endpoint - the
 * frontend passes this straight to {@code pushManager.subscribe({ applicationServerKey })}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VapidPublicKeyResponse {
    private String publicKey;
}
