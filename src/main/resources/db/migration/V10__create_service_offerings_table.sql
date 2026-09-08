-- ============================================================
-- Bookable service catalog
-- ============================================================
CREATE TABLE service_offerings (
    id               BIGSERIAL      PRIMARY KEY,
    name             VARCHAR(255)   NOT NULL,
    category         VARCHAR(50),
    description      TEXT,
    base_price       NUMERIC(10, 2),
    duration_minutes INTEGER,

    created_at       TIMESTAMP(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by       BIGINT,
    updated_by       BIGINT
);

CREATE INDEX idx_service_offerings_category ON service_offerings(category);
