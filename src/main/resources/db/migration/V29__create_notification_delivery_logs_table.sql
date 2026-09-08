-- ============================================================
-- Simulated multi-channel notification delivery log. SMS/email
-- rows are simulated (no real Twilio/SendGrid integration) —
-- see NotificationChannelEnum and NotificationServiceImpl.
-- ============================================================
CREATE TABLE notification_delivery_logs (
    id                   BIGSERIAL    PRIMARY KEY,
    notification_id      BIGINT       NOT NULL REFERENCES notifications(id) ON DELETE CASCADE,
    channel              VARCHAR(20)  NOT NULL,
    simulated_recipient  VARCHAR(255),
    dispatched_at        TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notification_delivery_logs_notification_id ON notification_delivery_logs(notification_id);
