-- ============================================================
-- Elder profiles managed by a family member
-- ============================================================
CREATE TABLE elder_profiles (
    id                       BIGSERIAL    PRIMARY KEY,
    family_user_id           BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name                     VARCHAR(255),
    date_of_birth            DATE,
    gender                   VARCHAR(20),
    medical_conditions       TEXT,
    address                  TEXT,
    emergency_contact_name   VARCHAR(255),
    emergency_contact_phone  VARCHAR(50),

    created_at               TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by               BIGINT,
    updated_by               BIGINT
);

CREATE INDEX idx_elder_profiles_family_user_id ON elder_profiles(family_user_id);
