package com.eldersphere.dtos.Notification;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for {@code DELETE /api/v1/users/me/push-subscriptions}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushUnsubscribeRequest {

    @NotBlank
    private String endpoint;
}
