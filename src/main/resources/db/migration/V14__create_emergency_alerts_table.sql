-- ============================================================
-- Emergency alerts triggered for an elder
-- ============================================================
CREATE TABLE emergency_alerts (
    id                       BIGSERIAL         PRIMARY KEY,
    elder_profile_id         BIGINT            NOT NULL REFERENCES elder_profiles(id) ON DELETE CASCADE,
    triggered_by_user_id     BIGINT            NOT NULL REFERENCES users(id),
    latitude                 DOUBLE PRECISION,
    longitude                DOUBLE PRECISION,
    status                   VARCHAR(50),
    responding_caretaker_id  BIGINT REFERENCES caretaker_profiles(id),
    triggered_at             TIMESTAMP(6),
    resolved_at              TIMESTAMP(6),
    response_time_seconds    INTEGER,

    created_at               TIMESTAMP(6)      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP(6)      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by               BIGINT,
    updated_by               BIGINT
);

CREATE INDEX idx_emergency_alerts_elder_profile_id ON emergency_alerts(elder_profile_id);
CREATE INDEX idx_emergency_alerts_status ON emergency_alerts(status);
