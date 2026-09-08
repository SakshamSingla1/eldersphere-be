-- ============================================================
-- Roles: admin-side RBAC roles (e.g. SUPER_ADMIN, SUPPORT_AGENT)
-- ============================================================
CREATE TABLE roles (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    status      VARCHAR(50),

    created_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  BIGINT,
    updated_by  BIGINT,

    CONSTRAINT uk_roles_name UNIQUE (name)
);
