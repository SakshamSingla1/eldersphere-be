-- ============================================================
-- Caretaker weekly availability: a caretaker can list multiple
-- time slots per day of week.
-- ============================================================
CREATE TABLE caretaker_availability (
    id             BIGSERIAL    PRIMARY KEY,
    caretaker_id   BIGINT       NOT NULL REFERENCES caretaker_profiles(id) ON DELETE CASCADE,
    day_of_week    VARCHAR(10)  NOT NULL,
    start_time     TIME         NOT NULL,
    end_time       TIME         NOT NULL,

    created_at     TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by     BIGINT,
    updated_by     BIGINT,

    CONSTRAINT chk_caretaker_availability_time CHECK (end_time > start_time)
);

CREATE INDEX idx_caretaker_availability_caretaker_id ON caretaker_availability(caretaker_id, day_of_week);
