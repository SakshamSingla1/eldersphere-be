-- ============================================================
-- Landing page hero/CTA config (single admin-editable row)
-- ============================================================
CREATE TABLE landing_page_config (
    id                  BIGSERIAL    PRIMARY KEY,
    hero_headline       VARCHAR(500),
    hero_subheadline    TEXT,
    hero_image_url      VARCHAR(1000),
    cta_headline        VARCHAR(500),
    cta_description     TEXT,
    cta_button_text     VARCHAR(255),

    created_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          BIGINT,
    updated_by          BIGINT
);
