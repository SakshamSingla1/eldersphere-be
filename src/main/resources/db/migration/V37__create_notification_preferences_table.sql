-- ============================================================
-- Per-user, per-notification-type delivery channel preferences.
-- A missing row means "use the default" (in-app on, email/SMS off)
-- - see NotificationPreferenceServiceImpl - so no backfill of one
-- row per existing user per type is needed.
-- ============================================================
CREATE TABLE notification_preferences (
    id                  BIGSERIAL    PRIMARY KEY,
    user_id             BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    notification_type   VARCHAR(50)  NOT NULL,
    in_app_enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    email_enabled       BOOLEAN      NOT NULL DEFAULT FALSE,
    sms_enabled         BOOLEAN      NOT NULL DEFAULT FALSE,

    created_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          BIGINT,
    updated_by          BIGINT,

    CONSTRAINT uq_notification_preference_user_type UNIQUE (user_id, notification_type)
);

CREATE INDEX idx_notification_preferences_user_id ON notification_preferences(user_id);
