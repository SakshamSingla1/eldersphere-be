-- ============================================================
-- Users: elders, family members, caretakers, and admin staff
-- ============================================================
CREATE TABLE users (
    id            BIGSERIAL    PRIMARY KEY,
    email         VARCHAR(255) NOT NULL,
    phone         VARCHAR(50),
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255),
    user_type     VARCHAR(50),
    status        VARCHAR(50),
    role_id       BIGINT REFERENCES roles(id) ON DELETE SET NULL,

    created_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by    BIGINT,
    updated_by    BIGINT,

    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE INDEX idx_users_user_type ON users(user_type);
CREATE INDEX idx_users_status ON users(status);
