-- ============================================================
-- Activates the pre-existing (previously dormant) Role/Permission/
-- RolePermission fine-grained admin RBAC system: seeds a permission
-- catalog covering every admin-panel nav module (VIEW/MANAGE grain),
-- two example restricted admin roles, and one demo account showing
-- restricted access end-to-end.
--
-- Enforcement (see AdminPermissionGuard, wired into @PreAuthorize on
-- the relevant admin controllers) only ever narrows access for an
-- ADMIN user who has been assigned a roleId - SUPER_ADMIN always has
-- full access regardless, and an ADMIN with roleId still NULL (every
-- existing admin account before this migration) keeps full access
-- exactly as before. This migration only adds catalog data and one
-- new demo user; it does not touch any existing user's roleId.
-- ============================================================

INSERT INTO permissions (name, description) VALUES
    ('USERS_VIEW', 'View admin user accounts'),
    ('USERS_MANAGE', 'Create, edit, suspend/delete and assign roles to user accounts'),
    ('CARETAKER_VERIFICATION_VIEW', 'View caretaker verification documents'),
    ('CARETAKER_VERIFICATION_MANAGE', 'Approve or reject caretaker verification'),
    ('ELDER_PROFILES_VIEW', 'View elder profiles'),
    ('ELDER_PROFILES_MANAGE', 'Edit or delete elder profiles'),
    ('SERVICES_VIEW', 'View the service offering catalog'),
    ('SERVICES_MANAGE', 'Create, edit and delete service offerings'),
    ('BOOKINGS_VIEW', 'View bookings'),
    ('BOOKINGS_MANAGE', 'Update booking status'),
    ('MEDICAL_RECORDS_VIEW', 'View medical records'),
    ('MEDICAL_RECORDS_MANAGE', 'Create, edit and delete medical records'),
    ('REVIEWS_VIEW', 'View caretaker reviews'),
    ('REVIEWS_MANAGE', 'Moderate caretaker reviews'),
    ('EMERGENCY_ALERTS_VIEW', 'View emergency alerts'),
    ('EMERGENCY_ALERTS_MANAGE', 'Acknowledge or resolve emergency alerts'),
    ('LANDING_MANAGEMENT_VIEW', 'View landing page admin content (features/FAQs/testimonials)'),
    ('LANDING_MANAGEMENT_MANAGE', 'Edit landing page config/features/FAQs/testimonials'),
    ('CONTACT_US_VIEW', 'View contact-us submissions'),
    ('CONTACT_US_MANAGE', 'Update or delete contact-us submissions'),
    ('ROLES_PERMISSIONS_VIEW', 'View roles and permissions'),
    ('ROLES_PERMISSIONS_MANAGE', 'Create/edit roles and assign permissions'),
    ('PLATFORM_SETTINGS_VIEW', 'View platform settings'),
    ('PLATFORM_SETTINGS_MANAGE', 'Edit platform settings'),
    ('ANALYTICS_VIEW', 'View analytics dashboards')
ON CONFLICT (name) DO NOTHING;

-- ------------------------------------------------------------
-- Example restricted role #1: Bookings Coordinator - day-to-day
-- operational scope only (bookings, the service catalog they map
-- to, and medical records needed to coordinate care). No access to
-- user management, platform settings, or roles/permissions.
-- ------------------------------------------------------------
INSERT INTO roles (name, description, status) VALUES
    ('Bookings Coordinator',
     'Coordinates bookings, the service catalog, and medical records. No access to user management, platform settings, analytics, or roles/permissions.',
     'ACTIVE')
ON CONFLICT (name) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'Bookings Coordinator'
  AND p.name IN ('BOOKINGS_VIEW', 'BOOKINGS_MANAGE', 'SERVICES_VIEW', 'SERVICES_MANAGE', 'MEDICAL_RECORDS_VIEW', 'MEDICAL_RECORDS_MANAGE')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ------------------------------------------------------------
-- Example restricted role #2: Content Manager - public-facing
-- content and the contact-us inbox only.
-- ------------------------------------------------------------
INSERT INTO roles (name, description, status) VALUES
    ('Content Manager',
     'Manages public-facing landing page content and the contact-us inbox. No access to user, booking, or medical data.',
     'ACTIVE')
ON CONFLICT (name) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.name = 'Content Manager'
  AND p.name IN ('LANDING_MANAGEMENT_VIEW', 'LANDING_MANAGEMENT_MANAGE', 'CONTACT_US_VIEW', 'CONTACT_US_MANAGE')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ------------------------------------------------------------
-- Demo account: an ADMIN whose roleId is assigned to Bookings
-- Coordinator, so there is a real account demonstrating restricted
-- admin access end-to-end (id 90006 - next free explicit demo id
-- after V25's 90001-90005).
-- ------------------------------------------------------------
-- Password123! (same bcrypt hash used by every other demo account)
INSERT INTO users (id, email, phone, password_hash, full_name, user_type, status, role_id)
SELECT 90006, 'roleassigned.admin@eldersphere.app', '+10000000006',
       '$2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe',
       'Bookings Coordinator Admin Demo', 'ADMIN', 'ACTIVE', r.id
FROM roles r
WHERE r.name = 'Bookings Coordinator'
ON CONFLICT (id) DO NOTHING;

INSERT INTO user_role_mappings (user_id, role_type, is_primary)
SELECT 90006, 'ADMIN', TRUE
WHERE EXISTS (SELECT 1 FROM users WHERE id = 90006)
ON CONFLICT (user_id, role_type) DO NOTHING;

SELECT setval('users_id_seq', (SELECT GREATEST(MAX(id), 1) FROM users));
