-- ============================================================
-- Medical records for an elder (prescriptions, treatment, labs)
-- ============================================================
CREATE TABLE medical_records (
    id                       BIGSERIAL    PRIMARY KEY,
    elder_profile_id         BIGINT       NOT NULL REFERENCES elder_profiles(id) ON DELETE CASCADE,
    type                     VARCHAR(50),
    title                    VARCHAR(255),
    document_file_asset_id   BIGINT,
    notes                    TEXT,
    shared_with_family       BOOLEAN      NOT NULL DEFAULT FALSE,

    created_at               TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by               BIGINT,
    updated_by               BIGINT
);

CREATE INDEX idx_medical_records_elder_profile_id ON medical_records(elder_profile_id);
