-- ============================================================
-- Landing page FAQs
-- ============================================================
CREATE TABLE landing_faqs (
    id          BIGSERIAL    PRIMARY KEY,
    question    TEXT,
    answer      TEXT,
    sort_order  INTEGER,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,

    created_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  BIGINT,
    updated_by  BIGINT
);

CREATE INDEX idx_landing_faqs_active_sort ON landing_faqs(is_active, sort_order);
