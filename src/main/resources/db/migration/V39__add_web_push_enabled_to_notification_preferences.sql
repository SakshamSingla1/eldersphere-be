-- ============================================================
-- Adds the WEB_PUSH channel toggle to the existing per-user,
-- per-notification-type preference row - see V37 for the original
-- table and NotificationPreferenceServiceImpl for the default
-- (off, same as email/SMS) applied when no row exists yet.
-- ============================================================
ALTER TABLE notification_preferences
    ADD COLUMN web_push_enabled BOOLEAN NOT NULL DEFAULT FALSE;
