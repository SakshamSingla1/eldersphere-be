-- ============================================================
-- Bookings: a family books a caretaker for an elder's service
-- ============================================================
CREATE TABLE bookings (
    id              BIGSERIAL      PRIMARY KEY,
    family_user_id  BIGINT         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    elder_profile_id BIGINT        NOT NULL REFERENCES elder_profiles(id) ON DELETE CASCADE,
    caretaker_id    BIGINT         NOT NULL REFERENCES caretaker_profiles(id) ON DELETE CASCADE,
    service_id      BIGINT         NOT NULL REFERENCES service_offerings(id),
    scheduled_date  DATE,
    scheduled_time  TIME,
    status          VARCHAR(50),
    cost            NUMERIC(10, 2),
    notes           TEXT,

    created_at      TIMESTAMP(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      BIGINT,
    updated_by      BIGINT
);

CREATE INDEX idx_bookings_family_user_id ON bookings(family_user_id);
CREATE INDEX idx_bookings_caretaker_id ON bookings(caretaker_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_caretaker_schedule ON bookings(caretaker_id, scheduled_date, scheduled_time);
