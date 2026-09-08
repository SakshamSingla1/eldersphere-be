package com.eldersphere.dtos.Notification;

import com.eldersphere.enums.NotificationTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single (notification type -> channel toggles) entry, used both for the resolved list
 * returned by GET and for the bulk-update request body of PUT
 * {@code /api/v1/users/me/notification-preferences}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceDTO {

    @NotNull
    private NotificationTypeEnum type;

    private boolean inAppEnabled;
    private boolean emailEnabled;
    private boolean smsEnabled;
    private boolean webPushEnabled;
}
