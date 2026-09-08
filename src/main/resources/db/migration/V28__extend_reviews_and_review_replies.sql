-- ============================================================
-- Richer reviews: rating breakdown (punctuality / care quality /
-- communication), an optional photo, and a single caretaker reply
-- per review.
-- ============================================================
ALTER TABLE reviews
    ADD COLUMN punctuality_rating   INTEGER,
    ADD COLUMN care_quality_rating  INTEGER,
    ADD COLUMN communication_rating INTEGER,
    ADD COLUMN photo_file_asset_id  BIGINT REFERENCES file_assets(id) ON DELETE SET NULL;

ALTER TABLE reviews
    ADD CONSTRAINT chk_review_punctuality_rating CHECK (punctuality_rating BETWEEN 1 AND 5),
    ADD CONSTRAINT chk_review_care_quality_rating CHECK (care_quality_rating BETWEEN 1 AND 5),
    ADD CONSTRAINT chk_review_communication_rating CHECK (communication_rating BETWEEN 1 AND 5);

CREATE TABLE review_replies (
    id           BIGSERIAL    PRIMARY KEY,
    review_id    BIGINT       NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    caretaker_id BIGINT       NOT NULL REFERENCES caretaker_profiles(id) ON DELETE CASCADE,
    content      TEXT         NOT NULL,

    created_at   TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by   BIGINT,
    updated_by   BIGINT,

    CONSTRAINT uk_review_reply_review UNIQUE (review_id)
);
