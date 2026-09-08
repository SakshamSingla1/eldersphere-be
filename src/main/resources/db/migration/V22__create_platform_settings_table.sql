-- ============================================================
-- Platform settings: singleton row (id = 1) of platform-wide config
-- ============================================================
CREATE TABLE platform_settings (
    id                               BIGINT       PRIMARY KEY,
    platform_name                    VARCHAR(255),
    support_email                    VARCHAR(255),
    support_phone                    VARCHAR(50),
    emergency_response_sla_minutes   INTEGER,

    created_at                       TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                       TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                       BIGINT,
    updated_by                       BIGINT
);

INSERT INTO platform_settings (id, platform_name, support_email, support_phone, emergency_response_sla_minutes)
VALUES (1, 'ElderSphere', 'support@eldersphere.app', NULL, 5);
