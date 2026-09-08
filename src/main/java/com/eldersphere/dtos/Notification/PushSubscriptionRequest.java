package com.eldersphere.dtos.Notification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for {@code POST /api/v1/users/me/push-subscriptions} - the exact shape a
 * browser's {@code PushSubscription.toJSON()} produces after a successful
 * {@code pushManager.subscribe(...)} call.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushSubscriptionRequest {

    @NotBlank
    private String endpoint;

    @NotNull
    @Valid
    private PushSubscriptionKeysDTO keys;
}
