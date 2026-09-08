-- ============================================================
-- Browser Web Push subscriptions (one row per subscribed browser/device
-- per user). Shape mirrors the standard PushSubscription object a
-- browser's Push API returns: { endpoint, keys: { p256dh, auth } }.
-- endpoint is globally unique (a given browser subscription can only
-- ever belong to one user at a time) so registration is idempotent.
-- ============================================================
CREATE TABLE push_subscriptions (
    id                  BIGSERIAL    PRIMARY KEY,
    user_id             BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    endpoint            TEXT         NOT NULL,
    p256dh_key          TEXT         NOT NULL,
    auth_key            TEXT         NOT NULL,

    created_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          BIGINT,
    updated_by          BIGINT,

    CONSTRAINT uq_push_subscription_endpoint UNIQUE (endpoint)
);

CREATE INDEX idx_push_subscriptions_user_id ON push_subscriptions(user_id);
