-- ============================================================
-- In-app notifications
-- ============================================================
CREATE TABLE notifications (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type        VARCHAR(50),
    title       VARCHAR(255),
    message     TEXT,
    is_read     BOOLEAN      NOT NULL DEFAULT FALSE,
    link        VARCHAR(500),

    created_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  BIGINT,
    updated_by  BIGINT
);

CREATE INDEX idx_notifications_user_id_created_at ON notifications(user_id, created_at DESC);
