-- ============================================================
-- Public contact-us form submissions
-- ============================================================
CREATE TABLE contact_us (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(255),
    email       VARCHAR(255),
    phone       VARCHAR(50),
    message     TEXT,
    status      VARCHAR(50),

    created_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  BIGINT,
    updated_by  BIGINT
);

CREATE INDEX idx_contact_us_status ON contact_us(status);
