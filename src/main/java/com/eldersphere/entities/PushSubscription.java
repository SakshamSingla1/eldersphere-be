package com.eldersphere.entities;

import com.eldersphere.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * One row per browser/device a user has subscribed to Web Push on. Shape mirrors the
 * standard {@code PushSubscription} object a browser's Push API returns:
 * {@code { endpoint, keys: { p256dh, auth } }}. {@code endpoint} is globally unique -
 * registering the same endpoint again (e.g. the browser re-registering on page load)
 * is idempotent, see {@link com.eldersphere.services.impl.PushSubscriptionServiceImpl}.
 */
@Entity
@Table(name = "push_subscriptions", uniqueConstraints =
        @UniqueConstraint(name = "uq_push_subscription_endpoint", columnNames = "endpoint"))
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushSubscription extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String endpoint;

    @Column(name = "p256dh_key", nullable = false, columnDefinition = "TEXT")
    private String p256dhKey;

    @Column(name = "auth_key", nullable = false, columnDefinition = "TEXT")
    private String authKey;
}
