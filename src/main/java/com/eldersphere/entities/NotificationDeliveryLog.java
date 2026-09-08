package com.eldersphere.entities;

import com.eldersphere.enums.NotificationChannelEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Records a (real or simulated) delivery attempt for a {@link Notification}. SMS/email
 * channels are simulated — see {@link NotificationChannelEnum} — so this is purely a DB
 * record of "we would have sent this" for demo/analytics purposes, no external provider call.
 */
@Entity
@Table(name = "notification_delivery_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannelEnum channel;

    @Column(name = "simulated_recipient")
    private String simulatedRecipient;

    @Column(name = "dispatched_at", nullable = false)
    private LocalDateTime dispatchedAt;
}
