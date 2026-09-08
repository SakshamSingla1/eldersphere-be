-- ============================================================
-- Caretaker profiles: bio, specialties, rate, verification
-- ============================================================
CREATE TABLE caretaker_profiles (
    id                          BIGSERIAL      PRIMARY KEY,
    user_id                     BIGINT         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    bio                         TEXT,
    years_of_experience         INTEGER,
    hourly_rate                 NUMERIC(10, 2),
    rating_average              DOUBLE PRECISION,
    verification_status         VARCHAR(50),
    profile_photo_file_asset_id BIGINT,

    created_at                  TIMESTAMP(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  BIGINT,
    updated_by                  BIGINT,

    CONSTRAINT uk_caretaker_profiles_user_id UNIQUE (user_id)
);

CREATE INDEX idx_caretaker_profiles_verification_status ON caretaker_profiles(verification_status);
