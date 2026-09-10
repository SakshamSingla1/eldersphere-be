-- Seeds nav_links with exactly the items each portal's frontend NAV_ITEMS array already
-- rendered (see FamilyRoutes.tsx / CaretakerRoutes.tsx / AdminRoutes.tsx / ElderRoutes.tsx
-- before this migration) — same labels, paths, icons, order, and admin permission gates —
-- so switching the sidebar to read from this table is a no-op for every existing user.
-- required_permission values match the exact @adminPermissionGuard.has(...) key already
-- enforced on that module's backend routes; NULL where no such gate exists today.

-- Admin (grouped: Overview / Care Operations / Content / Platform)
INSERT INTO nav_links (user_type, nav_group, nav_index, name, path, icon, required_permission, super_admin_only) VALUES
    ('ADMIN', 'Overview', 1, 'Dashboard', '/admin/dashboard', 'DashboardIcon', NULL, FALSE),
    ('ADMIN', 'Overview', 2, 'Analytics', '/admin/analytics', 'InsightsIcon', 'ANALYTICS_VIEW', FALSE),
    ('ADMIN', 'Care Operations', 3, 'Users', '/admin/users', 'PeopleIcon', 'USERS_VIEW', FALSE),
    ('ADMIN', 'Care Operations', 4, 'Caretaker Verification', '/admin/caretaker-verification', 'VerifiedUserIcon', 'CARETAKER_VERIFICATION_VIEW', FALSE),
    ('ADMIN', 'Care Operations', 5, 'Elder Profiles', '/admin/elder-profiles', 'ElderlyIcon', 'ELDER_PROFILES_VIEW', FALSE),
    ('ADMIN', 'Care Operations', 6, 'Services', '/admin/services', 'MedicalServicesIcon', NULL, FALSE),
    ('ADMIN', 'Care Operations', 7, 'Bookings', '/admin/bookings', 'EventNoteIcon', 'BOOKINGS_VIEW', FALSE),
    ('ADMIN', 'Care Operations', 8, 'Medical Records', '/admin/medical-records', 'FolderSharedIcon', 'MEDICAL_RECORDS_VIEW', FALSE),
    ('ADMIN', 'Care Operations', 9, 'Reviews', '/admin/reviews', 'RateReviewIcon', NULL, FALSE),
    ('ADMIN', 'Care Operations', 10, 'Emergency Alerts', '/admin/emergency-alerts', 'WarningAmberIcon', 'EMERGENCY_ALERTS_VIEW', FALSE),
    ('ADMIN', 'Content', 11, 'Landing Management', '/admin/landing-management', 'WebIcon', 'LANDING_MANAGEMENT_VIEW', FALSE),
    ('ADMIN', 'Content', 12, 'Contact Us', '/admin/contact-us', 'ContactMailIcon', 'CONTACT_US_VIEW', FALSE),
    ('ADMIN', 'Platform', 13, 'Roles & Permissions', '/admin/roles-permissions', 'AdminPanelSettingsIcon', NULL, TRUE),
    ('ADMIN', 'Platform', 14, 'Platform Settings', '/admin/platform-settings', 'TuneIcon', NULL, TRUE),
    ('ADMIN', 'Platform', 15, 'Navigation Links', '/admin/nav-links', 'ListAltIcon', NULL, TRUE);

-- Family (flat list, no groups)
INSERT INTO nav_links (user_type, nav_index, name, path, icon) VALUES
    ('FAMILY_MEMBER', 1, 'Dashboard', '/family/dashboard', 'DashboardIcon'),
    ('FAMILY_MEMBER', 2, 'Elder Profiles', '/family/elder-profiles', 'PeopleIcon'),
    ('FAMILY_MEMBER', 3, 'Invites', '/family/invites', 'MailOutlineIcon'),
    ('FAMILY_MEMBER', 4, 'Find a Caretaker', '/family/caretakers', 'SearchIcon'),
    ('FAMILY_MEMBER', 5, 'My Favorites', '/family/favorites', 'FavoriteIcon'),
    ('FAMILY_MEMBER', 6, 'My Bookings', '/family/bookings', 'EventNoteIcon'),
    ('FAMILY_MEMBER', 7, 'Medical Records', '/family/medical-records', 'FolderSharedIcon'),
    ('FAMILY_MEMBER', 8, 'Reviews', '/family/reviews', 'RateReviewIcon'),
    ('FAMILY_MEMBER', 9, 'Messages', '/family/messages', 'ChatBubbleOutlineIcon'),
    ('FAMILY_MEMBER', 10, 'Notifications', '/family/notifications', 'NotificationsIcon'),
    ('FAMILY_MEMBER', 11, 'Emergency', '/family/emergency', 'WarningAmberIcon'),
    ('FAMILY_MEMBER', 12, 'Settings', '/family/settings', 'SettingsIcon');

-- Caretaker (flat list, no groups)
INSERT INTO nav_links (user_type, nav_index, name, path, icon) VALUES
    ('CARETAKER', 1, 'Dashboard', '/caretaker/dashboard', 'DashboardIcon'),
    ('CARETAKER', 2, 'My Profile', '/caretaker/profile', 'BadgeIcon'),
    ('CARETAKER', 3, 'Bookings', '/caretaker/bookings', 'EventNoteIcon'),
    ('CARETAKER', 4, 'Reviews', '/caretaker/reviews', 'RateReviewIcon'),
    ('CARETAKER', 5, 'Messages', '/caretaker/messages', 'ChatBubbleOutlineIcon'),
    ('CARETAKER', 6, 'Notifications', '/caretaker/notifications', 'NotificationsIcon'),
    ('CARETAKER', 7, 'Settings', '/caretaker/settings', 'SettingsIcon');

-- Elder (flat list, no groups)
INSERT INTO nav_links (user_type, nav_index, name, path, icon) VALUES
    ('ELDER', 1, 'Dashboard', '/elder/dashboard', 'DashboardIcon'),
    ('ELDER', 2, 'My Profile', '/elder/profile', 'PersonIcon'),
    ('ELDER', 3, 'My Bookings', '/elder/bookings', 'EventNoteIcon'),
    ('ELDER', 4, 'Medical Records', '/elder/medical-records', 'FolderSharedIcon'),
    ('ELDER', 5, 'Invites', '/elder/invites', 'MailOutlineIcon'),
    ('ELDER', 6, 'Messages', '/elder/messages', 'ChatBubbleOutlineIcon'),
    ('ELDER', 7, 'Emergency', '/elder/emergency', 'WarningAmberIcon'),
    ('ELDER', 8, 'Notifications', '/elder/notifications', 'NotificationsIcon'),
    ('ELDER', 9, 'Settings', '/elder/settings', 'SettingsIcon');
