-- ============================================================
-- Family-elder linking by search + invite/accept, instead of a
-- family member only being able to type an elder's details from
-- scratch. Two new tables:
--
--   elder_profile_link_invites — a pending request to link an
--   existing user to an elder profile, either as the elder
--   (invited_role = ELDER, resolves onto elder_profiles.elder_user_id)
--   or as an additional co-managing family member (invited_role =
--   FAMILY_MEMBER, resolves into family_elder_links below). The
--   invited user must accept before any link is created.
--
--   family_elder_links — one or more ADDITIONAL family members
--   sharing management of an elder profile, on top of the
--   original elder_profiles.family_user_id owner. This is what
--   makes elder profiles shareable across more than one family
--   member; it did not exist before this migration.
-- ============================================================

CREATE TABLE elder_profile_link_invites (
    id                   BIGSERIAL    PRIMARY KEY,
    elder_profile_id     BIGINT       NOT NULL REFERENCES elder_profiles(id) ON DELETE CASCADE,
    invited_user_id      BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    invited_role         VARCHAR(32)  NOT NULL,
    invited_by_user_id   BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    relationship_label   VARCHAR(100),
    status               VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    responded_at         TIMESTAMP(6),

    created_at           TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by           BIGINT,
    updated_by           BIGINT
);

CREATE INDEX idx_elder_profile_link_invites_invited_user ON elder_profile_link_invites(invited_user_id, status);
CREATE INDEX idx_elder_profile_link_invites_profile ON elder_profile_link_invites(elder_profile_id, status);

CREATE TABLE family_elder_links (
    id                   BIGSERIAL    PRIMARY KEY,
    elder_profile_id     BIGINT       NOT NULL REFERENCES elder_profiles(id) ON DELETE CASCADE,
    family_user_id       BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    relationship_label   VARCHAR(100),
    invited_by_user_id   BIGINT       REFERENCES users(id) ON DELETE SET NULL,

    created_at           TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by           BIGINT,
    updated_by           BIGINT,

    CONSTRAINT uq_family_elder_links_profile_user UNIQUE (elder_profile_id, family_user_id)
);

CREATE INDEX idx_family_elder_links_family_user ON family_elder_links(family_user_id);
