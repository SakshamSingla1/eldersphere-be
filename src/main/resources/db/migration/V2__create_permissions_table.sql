-- ============================================================
-- Permissions: fine-grained admin RBAC permissions
-- ============================================================
CREATE TABLE permissions (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,

    created_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  BIGINT,
    updated_by  BIGINT,

    CONSTRAINT uk_permissions_name UNIQUE (name)
);
