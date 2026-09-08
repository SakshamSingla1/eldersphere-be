package com.eldersphere.enums;

/**
 * Delivery channel for a {@link com.eldersphere.entities.NotificationDeliveryLog} row.
 * SMS/email are simulated (DB record + INFO log line only) — mirrors the local-disk
 * simplification used for file storage elsewhere in this MVP; no real Twilio/SendGrid
 * integration is wired up. WEB_PUSH is real: a VAPID-authenticated Web Push message is
 * actually sent to each of the user's registered browser subscriptions — see
 * {@link com.eldersphere.webpush.WebPushSenderService}.
 */
public enum NotificationChannelEnum {
    IN_APP,
    SIMULATED_SMS,
    SIMULATED_EMAIL,
    WEB_PUSH
}
