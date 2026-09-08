package com.eldersphere.dtos.Notification;

import com.eldersphere.enums.NotificationChannelEnum;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDeliveryLogResponse {
    private Long id;
    private Long notificationId;
    private NotificationChannelEnum channel;
    private String simulatedRecipient;
    private LocalDateTime dispatchedAt;
}
