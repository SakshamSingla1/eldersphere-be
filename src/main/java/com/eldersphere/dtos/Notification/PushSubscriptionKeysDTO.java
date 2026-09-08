package com.eldersphere.dtos.Notification;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The {@code keys} object of a browser's Push API {@code PushSubscription.toJSON()} output.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushSubscriptionKeysDTO {

    @NotBlank
    private String p256dh;

    @NotBlank
    private String auth;
}
