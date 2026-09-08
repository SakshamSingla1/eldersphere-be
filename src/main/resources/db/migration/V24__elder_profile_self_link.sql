-- ============================================================
-- Elder profiles become linkable to the elder's own login
-- account, not just to a managing family member.
-- ============================================================

ALTER TABLE elder_profiles
    ADD COLUMN elder_user_id BIGINT REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE elder_profiles
    ALTER COLUMN family_user_id DROP NOT NULL;

ALTER TABLE elder_profiles
    ADD CONSTRAINT chk_elder_profiles_owner
        CHECK (family_user_id IS NOT NULL OR elder_user_id IS NOT NULL);

CREATE INDEX idx_elder_profiles_elder_user_id ON elder_profiles(elder_user_id);
