-- ============================================================
-- Saved/favorite caretakers: a family member can bookmark a
-- caretaker for quick re-booking later.
-- ============================================================
CREATE TABLE favorite_caretakers (
    id              BIGSERIAL    PRIMARY KEY,
    family_user_id  BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    caretaker_id    BIGINT       NOT NULL REFERENCES caretaker_profiles(id) ON DELETE CASCADE,

    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      BIGINT,
    updated_by      BIGINT,

    CONSTRAINT uq_favorite_caretakers_family_caretaker UNIQUE (family_user_id, caretaker_id)
);

CREATE INDEX idx_favorite_caretakers_family_user_id ON favorite_caretakers(family_user_id);
