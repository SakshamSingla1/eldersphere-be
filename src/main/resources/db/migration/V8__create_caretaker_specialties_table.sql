-- ============================================================
-- Caretaker specialties (element collection: NURSING, PHYSIOTHERAPY, etc.)
-- ============================================================
CREATE TABLE caretaker_specialties (
    caretaker_profile_id BIGINT       NOT NULL REFERENCES caretaker_profiles(id) ON DELETE CASCADE,
    specialty             VARCHAR(50) NOT NULL,

    CONSTRAINT uk_caretaker_specialty UNIQUE (caretaker_profile_id, specialty)
);

CREATE INDEX idx_caretaker_specialties_profile_id ON caretaker_specialties(caretaker_profile_id);
