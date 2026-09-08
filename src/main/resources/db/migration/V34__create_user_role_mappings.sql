-- ============================================================
-- Multi-role support: a user can hold more than one UserTypeEnum
-- value at once (e.g. FAMILY_MEMBER + CARETAKER), instead of the
-- single-role-per-account limitation that previously forced
-- separate accounts per role for the same person.
--
-- users.user_type remains the "primary/default role" — the role
-- a user lands on after login and the value all existing code
-- already reads. This table adds the full set of roles a user
-- holds; exactly one row per user has is_primary = true, and
-- that row's role_type must always match users.user_type
-- (enforced at the application layer, see UserRoleServiceImpl).
--
-- NOTE: this is UNRELATED to the pre-existing roles/permissions/
-- role_permissions tables, which are a separate fine-grained RBAC
-- system for admin-panel nav-item visibility. Hence "mapping" in
-- the name rather than "role" alone.
-- ============================================================
CREATE TABLE user_role_mappings (
    id                   BIGSERIAL    PRIMARY KEY,
    user_id              BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_type            VARCHAR(32)  NOT NULL,
    is_primary           BOOLEAN      NOT NULL DEFAULT FALSE,
    granted_by_user_id   BIGINT       REFERENCES users(id) ON DELETE SET NULL,

    created_at           TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by           BIGINT,
    updated_by           BIGINT,

    CONSTRAINT uq_user_role_mappings_user_role UNIQUE (user_id, role_type)
);

CREATE INDEX idx_user_role_mappings_user_id ON user_role_mappings(user_id);

-- Backfill: one row per existing user, using their current
-- users.user_type as their (sole, primary) role.
INSERT INTO user_role_mappings (user_id, role_type, is_primary)
SELECT id, user_type, TRUE FROM users;

-- ------------------------------------------------------------
-- Demo multi-role data: grant a few existing test accounts an
-- additional role on top of their primary one, so there's real
-- multi-role data to verify against and to build/test a
-- multi-role-aware frontend UI with.
-- ------------------------------------------------------------

-- sksingla135+family@gmail.com (id 91016): keep FAMILY_MEMBER as
-- primary, add CARETAKER. This is the account that previously
-- needed a separate sksingla135+caretaker@gmail.com login to
-- exercise the caretaker dashboard — no longer necessary.
--
-- NOTE: user 91016 exists only in some shared dev databases (added
-- out-of-band, not by any migration up to this point) — it is NOT
-- part of the reproducible V25/V30 seed data, so a fresh database
-- migrated from scratch won't have it yet. Guarded with WHERE EXISTS
-- (rather than a plain INSERT) so this migration still applies
-- cleanly on a brand-new database; it's a no-op there.
INSERT INTO user_role_mappings (user_id, role_type, is_primary)
SELECT 91016, 'CARETAKER', FALSE
WHERE EXISTS (SELECT 1 FROM users WHERE id = 91016)
ON CONFLICT (user_id, role_type) DO NOTHING;

-- family.demo@eldersphere.app (id 90004): keep FAMILY_MEMBER as
-- primary, add ELDER (a family member who also manages their own
-- elder profile directly).
INSERT INTO user_role_mappings (user_id, role_type, is_primary)
VALUES (90004, 'ELDER', FALSE)
ON CONFLICT (user_id, role_type) DO NOTHING;

-- caretaker.demo@eldersphere.app (id 90003): keep CARETAKER as
-- primary, add FAMILY_MEMBER (a caretaker who is also booking
-- care for their own relative).
INSERT INTO user_role_mappings (user_id, role_type, is_primary)
VALUES (90003, 'FAMILY_MEMBER', FALSE)
ON CONFLICT (user_id, role_type) DO NOTHING;

SELECT setval('user_role_mappings_id_seq', (SELECT GREATEST(MAX(id), 1) FROM user_role_mappings));
