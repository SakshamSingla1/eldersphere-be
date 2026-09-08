package com.eldersphere.entities;

import com.eldersphere.audit.Auditable;
import com.eldersphere.enums.NotificationTypeEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * One row per (user, {@link NotificationTypeEnum}) combination, recording which delivery
 * channels that user wants for that notification type. A missing row means the caller has
 * never customized this type - see {@link com.eldersphere.services.impl.NotificationPreferenceServiceImpl}
 * for the default (in-app on, email/SMS off) applied in that case.
 */
@Entity
@Table(name = "notification_preferences", uniqueConstraints =
        @UniqueConstraint(name = "uq_notification_preference_user_type", columnNames = {"user_id", "notification_type"}))
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationTypeEnum notificationType;

    @Column(name = "in_app_enabled", nullable = false)
    private boolean inAppEnabled;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled;

    @Column(name = "sms_enabled", nullable = false)
    private boolean smsEnabled;

    @Column(name = "web_push_enabled", nullable = false)
    private boolean webPushEnabled;
}
