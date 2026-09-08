-- ============================================================
-- Password reset tokens emailed to users on "forgot password"
-- ============================================================
CREATE TABLE password_reset_tokens (
    id          BIGSERIAL    PRIMARY KEY,
    token       VARCHAR(255) NOT NULL,
    user_id     BIGINT       REFERENCES users(id) ON DELETE CASCADE,
    expiry_date TIMESTAMP(6) NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,

    created_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  BIGINT,
    updated_by  BIGINT,

    CONSTRAINT uk_password_reset_tokens_token UNIQUE (token)
);

CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
