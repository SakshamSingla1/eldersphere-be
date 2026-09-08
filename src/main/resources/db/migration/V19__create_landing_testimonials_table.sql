-- ============================================================
-- Landing page curated testimonials (admin-editable homepage content;
-- distinct from the operational Review entity on completed bookings)
-- ============================================================
CREATE TABLE landing_testimonials (
    id           BIGSERIAL    PRIMARY KEY,
    author_name  VARCHAR(255),
    author_role  VARCHAR(255),
    content      TEXT,
    avatar_url   VARCHAR(1000),
    rating       INTEGER,
    sort_order   INTEGER,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,

    created_at   TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by   BIGINT,
    updated_by   BIGINT
);

CREATE INDEX idx_landing_testimonials_active_sort ON landing_testimonials(is_active, sort_order);
