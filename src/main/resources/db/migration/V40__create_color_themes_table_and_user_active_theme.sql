-- ============================================================
-- Dashboard color theme presets: a curated catalog (SUPER_ADMIN
-- CRUD via /api/v1/color-themes) of named MUI-shaped palettes any
-- authenticated user can pick as their own personal dashboard theme
-- (users.active_theme_id, nullable - null means "use the default").
--
-- palette is a single JSON blob (see ColorPalette/ColorPaletteConverter)
-- shaped to drop straight into MUI's createTheme({ palette: {...} }):
--   primary/secondary/error/warning/info/success: { main, light, dark, contrastText }
--   background: { default, paper }
--   text: { primary, secondary }
-- All 6 presets below were checked for WCAG AA contrast (>=4.5:1 for
-- main/contrastText button-label pairs, and each primary/secondary
-- main color checked against both a light (#FFFFFF) and dark
-- (#121212) surface using its own light/dark shade as appropriate)
-- rather than picked by eye.
-- ============================================================

CREATE TABLE color_themes (
    id          BIGSERIAL     PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL UNIQUE,
    palette     TEXT          NOT NULL,
    is_default  BOOLEAN       NOT NULL DEFAULT FALSE,
    status      VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',

    created_at  TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  BIGINT,
    updated_by  BIGINT
);

-- At most one theme is ever the default.
CREATE UNIQUE INDEX uq_color_themes_single_default ON color_themes (is_default) WHERE is_default = TRUE;

ALTER TABLE users ADD COLUMN active_theme_id BIGINT REFERENCES color_themes(id) ON DELETE SET NULL;

-- ------------------------------------------------------------
-- 1. Forest & Terracotta - the current default. Forest green primary,
--    terracotta secondary, warm off-white background.
-- ------------------------------------------------------------
INSERT INTO color_themes (name, palette, is_default, status) VALUES (
    'Forest & Terracotta',
    '{"primary":{"main":"#2F6F5E","light":"#4F8B7A","dark":"#1D4E40","contrastText":"#FFFFFF"},' ||
    '"secondary":{"main":"#E0875A","light":"#EAA57F","dark":"#B86A42","contrastText":"#1B1B1B"},' ||
    '"error":{"main":"#C62828","light":"#E57373","dark":"#8E0000","contrastText":"#FFFFFF"},' ||
    '"warning":{"main":"#ED6C02","light":"#FFB74D","dark":"#B53D00","contrastText":"#000000"},' ||
    '"info":{"main":"#01659C","light":"#4FA8D8","dark":"#013E61","contrastText":"#FFFFFF"},' ||
    '"success":{"main":"#2E7D32","light":"#66BB6A","dark":"#1B5E20","contrastText":"#FFFFFF"},' ||
    '"background":{"default":"#F7F4F0","paper":"#FFFFFF"},' ||
    '"text":{"primary":"#1E2A26","secondary":"#55645F"}}',
    TRUE, 'ACTIVE'
);

-- ------------------------------------------------------------
-- 2. Ocean Calm - blues/teals.
-- ------------------------------------------------------------
INSERT INTO color_themes (name, palette, is_default, status) VALUES (
    'Ocean Calm',
    '{"primary":{"main":"#1F6F8B","light":"#4A93AC","dark":"#124B61","contrastText":"#FFFFFF"},' ||
    '"secondary":{"main":"#2FB6A3","light":"#6FD1C2","dark":"#1D8577","contrastText":"#08312B"},' ||
    '"error":{"main":"#C62828","light":"#E57373","dark":"#8E0000","contrastText":"#FFFFFF"},' ||
    '"warning":{"main":"#ED6C02","light":"#FFB74D","dark":"#B53D00","contrastText":"#000000"},' ||
    '"info":{"main":"#01659C","light":"#4FA8D8","dark":"#013E61","contrastText":"#FFFFFF"},' ||
    '"success":{"main":"#2E7D32","light":"#66BB6A","dark":"#1B5E20","contrastText":"#FFFFFF"},' ||
    '"background":{"default":"#F2F8FA","paper":"#FFFFFF"},' ||
    '"text":{"primary":"#16262B","secondary":"#4F6B72"}}',
    FALSE, 'ACTIVE'
);

-- ------------------------------------------------------------
-- 3. Warm Sunset - warm oranges/corals.
-- ------------------------------------------------------------
INSERT INTO color_themes (name, palette, is_default, status) VALUES (
    'Warm Sunset',
    '{"primary":{"main":"#C6511F","light":"#EE8F6C","dark":"#8F3814","contrastText":"#FFFFFF"},' ||
    '"secondary":{"main":"#F2A65A","light":"#F6C084","dark":"#C77F35","contrastText":"#3A2200"},' ||
    '"error":{"main":"#C62828","light":"#E57373","dark":"#8E0000","contrastText":"#FFFFFF"},' ||
    '"warning":{"main":"#ED6C02","light":"#FFB74D","dark":"#B53D00","contrastText":"#000000"},' ||
    '"info":{"main":"#01659C","light":"#4FA8D8","dark":"#013E61","contrastText":"#FFFFFF"},' ||
    '"success":{"main":"#2E7D32","light":"#66BB6A","dark":"#1B5E20","contrastText":"#FFFFFF"},' ||
    '"background":{"default":"#FFF8F2","paper":"#FFFFFF"},' ||
    '"text":{"primary":"#3A2417","secondary":"#7A5B48"}}',
    FALSE, 'ACTIVE'
);

-- ------------------------------------------------------------
-- 4. Lavender Care - soft purples, fitting for a caregiving app.
-- ------------------------------------------------------------
INSERT INTO color_themes (name, palette, is_default, status) VALUES (
    'Lavender Care',
    '{"primary":{"main":"#7A5FA3","light":"#9C85BC","dark":"#5A4179","contrastText":"#FFFFFF"},' ||
    '"secondary":{"main":"#C98FB0","light":"#DBB0C8","dark":"#A56690","contrastText":"#34121F"},' ||
    '"error":{"main":"#C62828","light":"#E57373","dark":"#8E0000","contrastText":"#FFFFFF"},' ||
    '"warning":{"main":"#ED6C02","light":"#FFB74D","dark":"#B53D00","contrastText":"#000000"},' ||
    '"info":{"main":"#01659C","light":"#4FA8D8","dark":"#013E61","contrastText":"#FFFFFF"},' ||
    '"success":{"main":"#2E7D32","light":"#66BB6A","dark":"#1B5E20","contrastText":"#FFFFFF"},' ||
    '"background":{"default":"#F8F5FB","paper":"#FFFFFF"},' ||
    '"text":{"primary":"#2B2333","secondary":"#675D74"}}',
    FALSE, 'ACTIVE'
);

-- ------------------------------------------------------------
-- 5. Slate Professional - cooler grays/blues, more clinical.
-- ------------------------------------------------------------
INSERT INTO color_themes (name, palette, is_default, status) VALUES (
    'Slate Professional',
    '{"primary":{"main":"#37517E","light":"#6E8CB8","dark":"#253A5C","contrastText":"#FFFFFF"},' ||
    '"secondary":{"main":"#5C6B73","light":"#8B9AA2","dark":"#3E4A50","contrastText":"#FFFFFF"},' ||
    '"error":{"main":"#C62828","light":"#E57373","dark":"#8E0000","contrastText":"#FFFFFF"},' ||
    '"warning":{"main":"#ED6C02","light":"#FFB74D","dark":"#B53D00","contrastText":"#000000"},' ||
    '"info":{"main":"#01659C","light":"#4FA8D8","dark":"#013E61","contrastText":"#FFFFFF"},' ||
    '"success":{"main":"#2E7D32","light":"#66BB6A","dark":"#1B5E20","contrastText":"#FFFFFF"},' ||
    '"background":{"default":"#F4F6F8","paper":"#FFFFFF"},' ||
    '"text":{"primary":"#1D2732","secondary":"#57646E"}}',
    FALSE, 'ACTIVE'
);

-- ------------------------------------------------------------
-- 6. Rosewood Bloom - deep rosewood/berry primary with a sage green
--    secondary; warm and distinct from all of the above.
-- ------------------------------------------------------------
INSERT INTO color_themes (name, palette, is_default, status) VALUES (
    'Rosewood Bloom',
    '{"primary":{"main":"#9C3D54","light":"#C1738A","dark":"#742A3D","contrastText":"#FFFFFF"},' ||
    '"secondary":{"main":"#6B8F71","light":"#95B79A","dark":"#4E6B53","contrastText":"#142016"},' ||
    '"error":{"main":"#C62828","light":"#E57373","dark":"#8E0000","contrastText":"#FFFFFF"},' ||
    '"warning":{"main":"#ED6C02","light":"#FFB74D","dark":"#B53D00","contrastText":"#000000"},' ||
    '"info":{"main":"#01659C","light":"#4FA8D8","dark":"#013E61","contrastText":"#FFFFFF"},' ||
    '"success":{"main":"#2E7D32","light":"#66BB6A","dark":"#1B5E20","contrastText":"#FFFFFF"},' ||
    '"background":{"default":"#FBF4F5","paper":"#FFFFFF"},' ||
    '"text":{"primary":"#2E1A1F","secondary":"#6B5158"}}',
    FALSE, 'ACTIVE'
);
