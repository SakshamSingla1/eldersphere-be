-- Sidebar navigation items, previously a hardcoded array per portal in the frontend
-- (FamilyRoutes.tsx / CaretakerRoutes.tsx / AdminRoutes.tsx / ElderRoutes.tsx). Moving them
-- here lets an admin add/rename/reorder/hide items without a frontend deploy, and gives a
-- future gating concept (e.g. a subscription tier) a real row to hang off instead of a
-- source-code array — this migration only adds the plain nav-item shape; no such gating
-- column exists yet, by design (nothing to gate against today).
--
-- required_permission is a soft reference (matched by name, not a DB foreign key) to
-- permissions.name — when set, the item is only shown to an ADMIN whose assigned Role has
-- been granted that permission (see AdminPermissionGuard), same rule already enforced on
-- the underlying API route. NULL means "no fine-grained gate" (every user of user_type can
-- see it). super_admin_only is a separate, simpler flag for the handful of routes reserved
-- for SUPER_ADMIN outright (Roles & Permissions, Platform Settings) - unrelated to the
-- Permission catalog.
CREATE TABLE nav_links (
    id BIGSERIAL PRIMARY KEY,
    user_type VARCHAR(30) NOT NULL,
    nav_group VARCHAR(100),
    nav_index INTEGER NOT NULL DEFAULT 0,
    name VARCHAR(100) NOT NULL,
    path VARCHAR(200) NOT NULL,
    icon VARCHAR(60),
    required_permission VARCHAR(100),
    super_admin_only BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

CREATE INDEX idx_nav_links_user_type ON nav_links (user_type);
