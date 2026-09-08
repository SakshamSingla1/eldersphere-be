-- ============================================================
-- Local dev / testing seed data: one demo account per role,
-- all sharing the password "Password123!" (bcrypt-hashed the
-- same way AuthService hashes real passwords, via Spring's
-- BCryptPasswordEncoder default strength).
--
-- Explicit high IDs (90001+) are used so these inserts cannot
-- collide with existing live dev data (e.g. users.id = 1,
-- elder_profiles.id = 1). Sequences are advanced afterward so
-- normal application inserts keep working correctly.
-- ============================================================

-- Password123!
-- $2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe

INSERT INTO users (id, email, phone, password_hash, full_name, user_type, status)
VALUES
    (90001, 'superadmin@eldersphere.app', '+10000000001', '$2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe', 'Super Admin Demo', 'SUPER_ADMIN', 'ACTIVE'),
    (90002, 'admin@eldersphere.app',      '+10000000002', '$2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe', 'Admin Demo',       'ADMIN',       'ACTIVE'),
    (90003, 'caretaker.demo@eldersphere.app', '+10000000003', '$2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe', 'Caretaker Demo', 'CARETAKER', 'ACTIVE'),
    (90004, 'family.demo@eldersphere.app',    '+10000000004', '$2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe', 'Family Demo',    'FAMILY_MEMBER', 'ACTIVE'),
    (90005, 'elder.demo@eldersphere.app',     '+10000000005', '$2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe', 'Elder Demo',     'ELDER', 'ACTIVE');

INSERT INTO caretaker_profiles (id, user_id, bio, years_of_experience, hourly_rate, rating_average, verification_status)
VALUES
    (90001, 90003, 'Experienced and compassionate caretaker specializing in nursing and companion care for seniors.', 6, 25.00, 4.8, 'VERIFIED');

INSERT INTO caretaker_specialties (caretaker_profile_id, specialty)
VALUES
    (90001, 'NURSING'),
    (90001, 'COMPANION_CARE');

-- Family-managed elder profile (owned by the demo family member)
INSERT INTO elder_profiles (id, family_user_id, elder_user_id, name, date_of_birth, gender, medical_conditions, address, emergency_contact_name, emergency_contact_phone)
VALUES
    (90001, 90004, NULL, 'Eleanor Demo', '1948-05-12', 'FEMALE', 'Hypertension', '12 Maple Street, Springfield', 'Family Demo', '+10000000004');

-- Self-managed elder profile (owned directly by the demo elder)
INSERT INTO elder_profiles (id, family_user_id, elder_user_id, name, date_of_birth, gender, medical_conditions, address, emergency_contact_name, emergency_contact_phone)
VALUES
    (90002, NULL, 90005, 'Edward Demo', '1950-09-30', 'MALE', 'Type 2 diabetes', '48 Oak Avenue, Springfield', 'Family Demo', '+10000000004');

INSERT INTO service_offerings (id, name, category, description, base_price, duration_minutes)
VALUES
    (90001, 'Nursing Care', 'NURSING', 'In-home professional nursing care including vitals monitoring and medication administration.', 40.00, 60),
    (90002, 'Physiotherapy', 'PHYSIOTHERAPY', 'Mobility and strength rehabilitation sessions tailored to the elder''s needs.', 35.00, 45),
    (90003, 'Companion Care', 'COMPANION_CARE', 'Friendly companionship, light housekeeping, and assistance with daily activities.', 20.00, 60);

-- Sample completed booking linking the demo family's elder, the demo caretaker, and a service
INSERT INTO bookings (id, family_user_id, elder_profile_id, caretaker_id, service_id, scheduled_date, scheduled_time, status, cost, notes)
VALUES
    (90001, 90004, 90001, 90001, 90003, CURRENT_DATE - INTERVAL '7 days', '10:00:00', 'COMPLETED', 20.00, 'Weekly companion care visit.');

-- Advance sequences so subsequent application inserts don't collide with these explicit IDs
SELECT setval('users_id_seq', (SELECT GREATEST(MAX(id), 1) FROM users));
SELECT setval('caretaker_profiles_id_seq', (SELECT GREATEST(MAX(id), 1) FROM caretaker_profiles));
SELECT setval('elder_profiles_id_seq', (SELECT GREATEST(MAX(id), 1) FROM elder_profiles));
SELECT setval('service_offerings_id_seq', (SELECT GREATEST(MAX(id), 1) FROM service_offerings));
SELECT setval('bookings_id_seq', (SELECT GREATEST(MAX(id), 1) FROM bookings));
