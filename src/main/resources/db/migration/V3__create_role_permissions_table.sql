-- ============================================================
-- Role <-> Permission mapping
-- ============================================================
CREATE TABLE role_permissions (
    id            BIGSERIAL    PRIMARY KEY,
    role_id       BIGINT       NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id BIGINT       NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,

    created_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by    BIGINT,
    updated_by    BIGINT,

    CONSTRAINT uq_role_permission UNIQUE (role_id, permission_id)
);

CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
