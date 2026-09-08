-- ============================================================
-- User roles expand from 4 to 5: SUPER_ADMIN, ADMIN, ELDER,
-- CARETAKER, FAMILY_MEMBER (was FAMILY).
--
-- The `user_type` column is a plain VARCHAR(50) with no CHECK
-- constraint, so no constraint needs to be altered to allow the
-- new SUPER_ADMIN value or the renamed FAMILY_MEMBER value -
-- both already fit within the existing column definition.
-- ============================================================

-- There is already live data using the old 'FAMILY' value.
UPDATE users SET user_type = 'FAMILY_MEMBER' WHERE user_type = 'FAMILY';
