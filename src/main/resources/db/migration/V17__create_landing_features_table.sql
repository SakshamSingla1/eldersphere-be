-- ============================================================
-- Landing page feature cards
-- ============================================================
CREATE TABLE landing_features (
    id          BIGSERIAL    PRIMARY KEY,
    title       VARCHAR(255),
    description TEXT,
    icon_name   VARCHAR(100),
    sort_order  INTEGER,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,

    created_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  BIGINT,
    updated_by  BIGINT
);

CREATE INDEX idx_landing_features_active_sort ON landing_features(is_active, sort_order);
