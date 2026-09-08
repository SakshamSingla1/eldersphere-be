-- ============================================================
-- Reviews: family rates a caretaker after a completed booking
-- ============================================================
CREATE TABLE reviews (
    id          BIGSERIAL    PRIMARY KEY,
    booking_id  BIGINT       NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    reviewer_id BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    caretaker_id BIGINT      NOT NULL REFERENCES caretaker_profiles(id) ON DELETE CASCADE,
    rating      INTEGER      NOT NULL,
    comment     TEXT,

    created_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  BIGINT,
    updated_by  BIGINT,

    CONSTRAINT uk_review_booking UNIQUE (booking_id),
    CONSTRAINT chk_review_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_reviews_caretaker_id ON reviews(caretaker_id);
