-- ============================================================
-- Richer demo/dev seed data on top of V25's five demo accounts
-- (which are left completely untouched). Adds enough caretakers,
-- families, elders, bookings, reviews, notifications and landing
-- content that the admin analytics endpoints and every dashboard
-- show real, varied numbers instead of ~1 row of everything.
--
-- Same convention as V25: explicit high IDs (91000+, past V25's
-- 90000-range) so these inserts cannot collide with live data,
-- with sequences advanced afterward.
--
-- Password123! (same bcrypt hash V25 uses for all its demo users)
-- $2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe
-- ============================================================

-- ------------------------------------------------------------
-- 9 additional caretaker users + profiles (7 VERIFIED, 2 PENDING)
-- ------------------------------------------------------------
INSERT INTO users (id, email, phone, password_hash, full_name, user_type, status)
VALUES
    (91001, 'rachel.kim@eldersphere.app',     '+15550001001', '$2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe', 'Rachel Kim',       'CARETAKER', 'ACTIVE'),
    (91002, 'marcus.ade@eldersphere.app',     '+15550001002', '$2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe', 'Marcus Ade',       'CARETAKER', 'ACTIVE'),
    (91003, 'priya.nair@eldersphere.app',     '+15550001003', '$2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe', 'Priya Nair',       'CARETAKER', 'ACTIVE'),
    (91004, 'diego.alvarez@eldersphere.app',  '+15550001004', '$2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe', 'Diego Alvarez',    'CARETAKER', 'ACTIVE'),
    (91005, 'grace.osei@eldersphere.app',     '+15550001005', '$2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe', 'Grace Osei',       'CARETAKER', 'ACTIVE'),
    (91006, 'tom.whitfield@eldersphere.app',  '+15550001006', '$2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe', 'Tom Whitfield',    'CARETAKER', 'ACTIVE'),
    (91007, 'elena.petrova@eldersphere.app',  '+15550001007', '$2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe', 'Elena Petrova',    'CARETAKER', 'ACTIVE'),
    (91008, 'samuel.idowu@eldersphere.app',   '+15550001008', '$2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe', 'Samuel Idowu',     'CARETAKER', 'ACTIVE'),
    (91009, 'aisha.malik@eldersphere.app',    '+15550001009', '$2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe', 'Aisha Malik',      'CARETAKER', 'ACTIVE');

INSERT INTO caretaker_profiles (id, user_id, bio, years_of_experience, hourly_rate, rating_average, verification_status)
VALUES
    (91001, 91001, 'Registered nurse focused on post-surgery recovery and vitals monitoring.', 8,  28.00, 4.6, 'VERIFIED'),
    (91002, 91002, 'Nurse and medication management specialist for chronic conditions.',        12, 32.50, 4.9, 'VERIFIED'),
    (91003, 91003, 'Licensed physiotherapist helping seniors regain mobility and strength.',     6,  30.00, 4.7, 'VERIFIED'),
    (91004, 91004, 'Physiotherapy and light companion care, bilingual (English/Spanish).',        4,  26.00, 4.3, 'VERIFIED'),
    (91005, 91005, 'Medication administration and adherence coaching specialist.',                9,  29.00, 4.8, 'VERIFIED'),
    (91006, 91006, 'Warm, patient companion caregiver - conversation, meals, light errands.',     3,  18.00, 4.1, 'VERIFIED'),
    (91007, 91007, 'Senior nurse with 15 years in home care nursing and companionship.',         15, 34.00, 5.0, 'VERIFIED'),
    (91008, 91008, 'Newly onboarded caretaker awaiting verification - medication + physiotherapy support.', 2, 22.00, 0.0, 'PENDING'),
    (91009, 91009, 'Newly onboarded companion caregiver awaiting verification.',                  1,  20.00, 0.0, 'PENDING');

INSERT INTO caretaker_specialties (caretaker_profile_id, specialty)
VALUES
    (91001, 'NURSING'),
    (91002, 'NURSING'),
    (91002, 'MEDICATION_ASSISTANCE'),
    (91003, 'PHYSIOTHERAPY'),
    (91004, 'PHYSIOTHERAPY'),
    (91004, 'COMPANION_CARE'),
    (91005, 'MEDICATION_ASSISTANCE'),
    (91006, 'COMPANION_CARE'),
    (91007, 'NURSING'),
    (91007, 'COMPANION_CARE'),
    (91008, 'MEDICATION_ASSISTANCE'),
    (91008, 'PHYSIOTHERAPY'),
    (91009, 'COMPANION_CARE');

INSERT INTO caretaker_availability (id, caretaker_id, day_of_week, start_time, end_time)
VALUES
    (91001, 91001, 'MONDAY',    '08:00', '16:00'),
    (91002, 91001, 'WEDNESDAY', '08:00', '16:00'),
    (91003, 91001, 'FRIDAY',    '08:00', '12:00'),
    (91004, 91002, 'TUESDAY',   '09:00', '17:00'),
    (91005, 91002, 'THURSDAY',  '09:00', '17:00'),
    (91006, 91003, 'MONDAY',    '10:00', '14:00'),
    (91007, 91003, 'WEDNESDAY', '10:00', '14:00'),
    (91008, 91003, 'FRIDAY',    '10:00', '14:00'),
    (91009, 91004, 'TUESDAY',   '08:00', '12:00'),
    (91010, 91004, 'THURSDAY',  '08:00', '12:00'),
    (91011, 91005, 'MONDAY',    '13:00', '18:00'),
    (91012, 91005, 'WEDNESDAY', '13:00', '18:00'),
    (91013, 91005, 'FRIDAY',    '13:00', '18:00'),
    (91014, 91006, 'SATURDAY',  '09:00', '17:00'),
    (91015, 91006, 'SUNDAY',    '09:00', '17:00'),
    (91016, 91007, 'MONDAY',    '07:00', '15:00'),
    (91017, 91007, 'TUESDAY',   '07:00', '15:00'),
    (91018, 91007, 'WEDNESDAY', '07:00', '15:00'),
    (91019, 91008, 'THURSDAY',  '09:00', '13:00'),
    (91020, 91008, 'FRIDAY',    '09:00', '13:00'),
    (91021, 91009, 'SATURDAY',  '10:00', '16:00');

-- ------------------------------------------------------------
-- 6 additional family members, each with 1-2 elder profiles
-- ------------------------------------------------------------
INSERT INTO users (id, email, phone, password_hash, full_name, user_type, status)
VALUES
    (91010, 'karen.lopez@example.com',   '+15550002001', '$2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe', 'Karen Lopez',   'FAMILY_MEMBER', 'ACTIVE'),
    (91011, 'james.turner@example.com',  '+15550002002', '$2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe', 'James Turner',  'FAMILY_MEMBER', 'ACTIVE'),
    (91012, 'fatima.rahman@example.com', '+15550002003', '$2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe', 'Fatima Rahman', 'FAMILY_MEMBER', 'ACTIVE'),
    (91013, 'oliver.brooks@example.com', '+15550002004', '$2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe', 'Oliver Brooks', 'FAMILY_MEMBER', 'ACTIVE'),
    (91014, 'ngozi.eze@example.com',     '+15550002005', '$2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe', 'Ngozi Eze',     'FAMILY_MEMBER', 'ACTIVE'),
    (91015, 'liam.fischer@example.com',  '+15550002006', '$2b$10$RwDbixTIX3xNOsCeloMkAOIyrYsfFO3sDb1RmnqnvS7RNTQHUVrGe', 'Liam Fischer',  'FAMILY_MEMBER', 'ACTIVE');

INSERT INTO elder_profiles (id, family_user_id, name, date_of_birth, gender, medical_conditions, address, emergency_contact_name, emergency_contact_phone)
VALUES
    (91001, 91010, 'Dorothy Lopez',   '1942-03-11', 'FEMALE', 'Arthritis, mild hearing loss',       '4 Birchwood Lane, Riverton',   'Karen Lopez',   '+15550002001'),
    (91002, 91010, 'Harold Lopez',    '1939-11-02', 'MALE',   'Type 2 diabetes',                    '4 Birchwood Lane, Riverton',   'Karen Lopez',   '+15550002001'),
    (91003, 91011, 'Margaret Turner', '1945-07-19', 'FEMALE', 'Osteoporosis',                       '19 Elm Court, Riverton',       'James Turner',  '+15550002002'),
    (91004, 91012, 'Abdul Rahman',    '1938-01-27', 'MALE',   'Hypertension, early-stage dementia', '87 Cedar Ave, Millbrook',      'Fatima Rahman', '+15550002003'),
    (91005, 91012, 'Yasmin Rahman',   '1950-06-05', 'FEMALE', 'Chronic back pain',                  '87 Cedar Ave, Millbrook',      'Fatima Rahman', '+15550002003'),
    (91006, 91013, 'Walter Brooks',   '1941-09-14', 'MALE',   'COPD',                                '3 Hilltop Rd, Millbrook',      'Oliver Brooks', '+15550002004'),
    (91007, 91014, 'Chinwe Eze',      '1947-02-23', 'FEMALE', 'Hypertension',                       '56 Lakeside Dr, Fairview',      'Ngozi Eze',     '+15550002005'),
    (91008, 91014, 'Emeka Eze',       '1943-12-08', 'MALE',   'Type 2 diabetes, mild arthritis',    '56 Lakeside Dr, Fairview',      'Ngozi Eze',     '+15550002005'),
    (91009, 91015, 'Greta Fischer',   '1944-05-30', 'FEMALE', 'Mild dementia',                       '21 Orchard St, Fairview',       'Liam Fischer',  '+15550002006');

-- ------------------------------------------------------------
-- Two additional bookable services (fills out the 4th category
-- and adds a longer/overnight option for cost variety)
-- ------------------------------------------------------------
INSERT INTO service_offerings (id, name, category, description, base_price, duration_minutes)
VALUES
    (91001, 'Medication Assistance', 'MEDICATION_ASSISTANCE', 'Medication reminders, administration, and adherence tracking.', 25.00, 30),
    (91002, 'Overnight Companion Care', 'COMPANION_CARE', 'Overnight companionship and safety supervision.', 150.00, 480);

-- ------------------------------------------------------------
-- 30 bookings spread across the last ~60 days (mostly COMPLETED,
-- some CONFIRMED/PENDING, a few CANCELLED) driving the analytics
-- time-series and revenue charts.
-- ------------------------------------------------------------
INSERT INTO bookings (id, family_user_id, elder_profile_id, caretaker_id, service_id, scheduled_date, scheduled_time, status, cost, notes)
VALUES
    (91001, 90004, 90001, 91001, 90001, CURRENT_DATE - INTERVAL '58 days', '09:00:00', 'COMPLETED', 80.00,  'Post-op vitals check.'),
    (91002, 91010, 91001, 91002, 91001, CURRENT_DATE - INTERVAL '55 days', '14:00:00', 'COMPLETED', 25.00,  'Morning medication administration.'),
    (91003, 91010, 91002, 91003, 90002, CURRENT_DATE - INTERVAL '52 days', '11:00:00', 'COMPLETED', 35.00,  'Mobility session.'),
    (91004, 91011, 91003, 91004, 90003, CURRENT_DATE - INTERVAL '50 days', '10:00:00', 'COMPLETED', 40.00,  'Companion visit, grocery run.'),
    (91005, 91012, 91004, 91005, 90001, CURRENT_DATE - INTERVAL '47 days', '08:30:00', 'COMPLETED', 40.00,  'Vitals monitoring.'),
    (91006, 91012, 91005, 91006, 90003, CURRENT_DATE - INTERVAL '45 days', '15:00:00', 'COMPLETED', 20.00,  'Afternoon companionship.'),
    (91007, 91013, 91006, 91007, 91002, CURRENT_DATE - INTERVAL '42 days', '18:00:00', 'COMPLETED', 150.00, 'Overnight supervision.'),
    (91008, 91014, 91007, 90001, 90002, CURRENT_DATE - INTERVAL '40 days', '09:30:00', 'COMPLETED', 70.00,  'Strength rehab session.'),
    (91009, 91014, 91008, 91001, 90001, CURRENT_DATE - INTERVAL '37 days', '13:00:00', 'COMPLETED', 40.00,  'Weekly nursing check-in.'),
    (91010, 91015, 91009, 91002, 91001, CURRENT_DATE - INTERVAL '35 days', '10:00:00', 'COMPLETED', 25.00,  'Medication review.'),
    (91011, 90004, 90001, 91003, 90002, CURRENT_DATE - INTERVAL '33 days', '16:00:00', 'CANCELLED', 35.00,  'Family cancelled - rescheduling.'),
    (91012, 91010, 91001, 91004, 90003, CURRENT_DATE - INTERVAL '32 days', '09:00:00', 'COMPLETED', 60.00,  'Extended companion visit.'),
    (91013, 91010, 91002, 91005, 90001, CURRENT_DATE - INTERVAL '29 days', '11:30:00', 'COMPLETED', 80.00,  'Nursing visit, wound care.'),
    (91014, 91011, 91003, 91006, 91001, CURRENT_DATE - INTERVAL '26 days', '14:30:00', 'COMPLETED', 25.00,  'Medication reminder visit.'),
    (91015, 91012, 91004, 91007, 90003, CURRENT_DATE - INTERVAL '23 days', '10:00:00', 'COMPLETED', 20.00,  'Companion visit.'),
    (91016, 91012, 91005, 90001, 90001, CURRENT_DATE - INTERVAL '20 days', '08:00:00', 'COMPLETED', 40.00,  'Vitals check.'),
    (91017, 91013, 91006, 91001, 90002, CURRENT_DATE - INTERVAL '19 days', '12:00:00', 'CANCELLED', 35.00,  'Caretaker unavailable, cancelled.'),
    (91018, 91014, 91007, 91002, 91002, CURRENT_DATE - INTERVAL '17 days', '17:00:00', 'COMPLETED', 150.00, 'Overnight care.'),
    (91019, 91014, 91008, 91003, 90001, CURRENT_DATE - INTERVAL '12 days', '09:00:00', 'COMPLETED', 40.00,  'Nursing check-in.'),
    (91020, 91015, 91009, 91004, 90003, CURRENT_DATE - INTERVAL '9 days',  '15:30:00', 'CANCELLED', 20.00,  'Family rescheduled.'),
    (91021, 90004, 90001, 91005, 91001, CURRENT_DATE - INTERVAL '7 days',  '10:00:00', 'COMPLETED', 25.00,  'Medication administration.'),
    (91022, 91010, 91001, 91006, 90001, CURRENT_DATE - INTERVAL '5 days',  '09:00:00', 'CONFIRMED', 80.00,  'Scheduled nursing visit.'),
    (91023, 91010, 91002, 91007, 90002, CURRENT_DATE - INTERVAL '4 days',  '11:00:00', 'CONFIRMED', 35.00,  'Scheduled physiotherapy.'),
    (91024, 91011, 91003, 90001, 90003, CURRENT_DATE - INTERVAL '3 days',  '14:00:00', 'CONFIRMED', 40.00,  'Scheduled companion visit.'),
    (91025, 91012, 91004, 91001, 90001, CURRENT_DATE - INTERVAL '2 days',  '08:30:00', 'CONFIRMED', 40.00,  'Scheduled vitals check.'),
    (91026, 91012, 91005, 91002, 91001, CURRENT_DATE - INTERVAL '1 days',  '13:00:00', 'CONFIRMED', 25.00,  'Scheduled medication visit.'),
    (91027, 91013, 91006, 91003, 90002, CURRENT_DATE - INTERVAL '1 days',  '16:00:00', 'PENDING',   35.00,  'Awaiting caretaker confirmation.'),
    (91028, 91014, 91007, 91004, 90003, CURRENT_DATE - INTERVAL '2 days',  '09:30:00', 'PENDING',   20.00,  'Awaiting caretaker confirmation.'),
    (91029, 91014, 91008, 91005, 90001, CURRENT_DATE,                     '10:00:00', 'PENDING',   40.00,  'Awaiting caretaker confirmation.'),
    (91030, 91015, 91009, 91006, 91002, CURRENT_DATE - INTERVAL '1 days',  '18:00:00', 'PENDING',   150.00, 'Awaiting caretaker confirmation.');

-- ------------------------------------------------------------
-- Reviews (with the new rating-breakdown columns) for most
-- COMPLETED bookings, plus a handful of caretaker replies.
-- ------------------------------------------------------------
INSERT INTO reviews (id, booking_id, reviewer_id, caretaker_id, rating, comment, punctuality_rating, care_quality_rating, communication_rating)
VALUES
    (91001, 91001, 90004,  91001, 5, 'Rachel was fantastic - very attentive after the surgery.', 5, 5, 4),
    (91002, 91002, 91010, 91002, 5, 'Marcus is extremely reliable with medication timing.',      5, 5, 5),
    (91003, 91003, 91010, 91003, 4, 'Good session, elder felt noticeably better afterwards.',    4, 4, 4),
    (91004, 91004, 91011, 91004, 4, 'Friendly and helpful, arrived a little late.',               3, 4, 5),
    (91005, 91005, 91012, 91005, 5, 'Grace is thorough and explains everything clearly.',        5, 5, 5),
    (91006, 91006, 91012, 91006, 4, 'Pleasant company for my father.',                            4, 4, 4),
    (91007, 91007, 91013, 91007, 5, 'Elena went above and beyond overnight, highly recommend.',  5, 5, 5),
    (91008, 91008, 91014, 90001, 4, 'Solid physiotherapy session, good progress.',                4, 4, 3),
    (91009, 91009, 91014, 91001, 5, 'Very professional and caring nurse.',                        5, 5, 5),
    (91010, 91010, 91015, 91002, 4, 'Helpful medication review, would book again.',                4, 4, 4),
    (91011, 91012, 91010, 91004, 3, 'Decent visit but communication could improve.',              3, 3, 2),
    (91012, 91013, 91010, 91005, 5, 'Excellent wound care, very gentle with my mother.',          5, 5, 5),
    (91013, 91014, 91011, 91006, 4, 'Reliable and on time every visit.',                           5, 4, 4),
    (91014, 91015, 91012, 91007, 5, 'Wonderful companion, my father looks forward to visits.',    5, 5, 5),
    (91015, 91019, 91014, 91003, 4, 'Good nursing check-in, thorough notes left for family.',      4, 4, 4);

INSERT INTO review_replies (id, review_id, caretaker_id, content)
VALUES
    (91001, 91001, 91001, 'Thank you so much - it was a pleasure helping with the recovery!'),
    (91002, 91003, 91003, 'Glad to hear it - looking forward to the next session.'),
    (91003, 91007, 91007, 'Thank you for the kind words, happy to keep supporting your family.'),
    (91004, 91012, 91005, 'Appreciate the feedback, will bring extra supplies next time.');

-- ------------------------------------------------------------
-- A spread of notifications across users, mixed read/unread.
-- ------------------------------------------------------------
INSERT INTO notifications (id, user_id, type, title, message, is_read, link)
VALUES
    (91001, 90004,  'BOOKING_CONFIRMED', 'Booking confirmed', 'Your nursing visit has been confirmed.', TRUE,  NULL),
    (91002, 90003,  'BOOKING_CONFIRMED', 'New booking request', 'You have a new booking request.', FALSE, NULL),
    (91003, 91010, 'BOOKING_REMINDER',  'Upcoming visit', 'Reminder: nursing visit tomorrow at 9:00 AM.', FALSE, NULL),
    (91004, 91001, 'BOOKING_CONFIRMED', 'New booking request', 'You have a new booking request for Nursing Care.', TRUE,  NULL),
    (91005, 91011, 'BOOKING_CONFIRMED', 'Booking confirmed', 'Your companion care visit has been confirmed.', TRUE,  NULL),
    (91006, 91002, 'BOOKING_REMINDER',  'Upcoming visit', 'Reminder: medication visit in 2 days.', FALSE, NULL),
    (91007, 91012, 'EMERGENCY_ALERT',   'Emergency alert resolved', 'The emergency alert for Abdul Rahman has been resolved.', FALSE, NULL),
    (91008, 90005,  'GENERAL',           'Welcome to ElderSphere', 'Your account is fully set up.', TRUE,  NULL),
    (91009, 91013, 'NEW_MESSAGE',       'New message from Priya Nair', 'Just confirming tomorrow''s appointment time.', FALSE, NULL),
    (91010, 91003, 'NEW_MESSAGE',       'New message from Margaret''s family', 'Thank you for the update!', TRUE,  NULL),
    (91011, 91014, 'BOOKING_CONFIRMED', 'Booking confirmed', 'Your overnight care booking has been confirmed.', TRUE,  NULL),
    (91012, 91004, 'BOOKING_REMINDER',  'Upcoming visit', 'Reminder: physiotherapy session tomorrow.', FALSE, NULL),
    (91013, 91015, 'GENERAL',           'Profile updated', 'Your elder profile was updated successfully.', TRUE,  NULL),
    (91014, 91005, 'BOOKING_CONFIRMED', 'New booking request', 'You have a new booking request for Medication Assistance.', FALSE, NULL),
    (91015, 90004,  'GENERAL',           'Platform update', 'We have added in-app messaging and availability scheduling.', FALSE, NULL),
    (91016, 91006, 'BOOKING_CONFIRMED', 'New booking request', 'You have a new booking request for Companion Care.', TRUE,  NULL),
    (91017, 91010, 'NEW_MESSAGE',       'New message from Rachel Kim', 'See you at 9 AM tomorrow.', FALSE, NULL),
    (91018, 91007, 'BOOKING_CONFIRMED', 'Booking confirmed', 'Your nursing visit has been confirmed.', TRUE,  NULL),
    (91019, 91011, 'BOOKING_REMINDER',  'Upcoming visit', 'Reminder: companion visit this week.', FALSE, NULL),
    (91020, 91008, 'GENERAL',           'Verification pending', 'Your caretaker profile is under review by our team.', TRUE,  NULL);

-- ------------------------------------------------------------
-- Landing page CMS content (previously empty - the frontend was
-- falling back to hardcoded marketing copy without this).
-- ------------------------------------------------------------
INSERT INTO landing_page_config (id, hero_headline, hero_subheadline, hero_image_url, cta_headline, cta_description, cta_button_text)
VALUES
    (91001,
     'Trusted elder care, on your schedule',
     'ElderSphere connects families with verified, background-checked caretakers for nursing, physiotherapy, medication assistance, and companionship - book in minutes, stay connected in real time.',
     'https://images.eldersphere.app/hero-family.jpg',
     'Ready to find the right caretaker?',
     'Join thousands of families who trust ElderSphere for compassionate, reliable elder care.',
     'Get started free');

INSERT INTO landing_features (id, title, description, icon_name, sort_order, is_active)
VALUES
    (91001, 'Verified caretakers',        'Every caretaker is background-checked and verified by our admin team before appearing in search.', 'shield-check',  1, TRUE),
    (91002, 'Real-time messaging',        'Chat directly with your caretaker or family member, with instant delivery over WebSocket.',          'message-circle', 2, TRUE),
    (91003, 'Flexible scheduling',        'Caretakers publish their weekly availability so you always book a time that actually works.',        'calendar-clock', 3, TRUE),
    (91004, 'Emergency alerts',           'One tap notifies family and nearby caretakers immediately, with response-time tracking.',              'alert-triangle', 4, TRUE),
    (91005, 'Shared medical records',     'Keep prescriptions, treatment notes, and lab reports in one place, shareable with family on demand.',   'file-heart',     5, TRUE),
    (91006, 'Transparent reviews',        'Ratings cover punctuality, care quality, and communication - not just a single star score.',            'star',           6, TRUE),
    (91007, 'Admin oversight',            'Platform admins monitor bookings, revenue, and caretaker performance from a live analytics dashboard.', 'bar-chart-3',    7, TRUE);

INSERT INTO landing_faqs (id, question, answer, sort_order, is_active)
VALUES
    (91001, 'How are caretakers verified?', 'Every caretaker submits credentials that our admin team reviews before their profile is marked VERIFIED and becomes bookable.', 1, TRUE),
    (91002, 'Can I message my caretaker directly?', 'Yes - once a conversation is started (optionally tied to a booking), messages are delivered in real time in-app.', 2, TRUE),
    (91003, 'What happens during an emergency alert?', 'Triggering an alert immediately notifies the elder''s family and logs a response-time SLA that admins can monitor.', 3, TRUE),
    (91004, 'How is pricing determined?', 'Each service offering has a base price; caretakers also set their own hourly rate, shown before you confirm a booking.', 4, TRUE),
    (91005, 'Can family members share medical records?', 'Yes, medical records can be marked shared so family members and caretakers can view relevant history.', 5, TRUE),
    (91006, 'Is there a mobile app?', 'ElderSphere is a responsive web app today; native apps are on our roadmap.', 6, TRUE);

INSERT INTO landing_testimonials (id, author_name, author_role, content, avatar_url, rating, sort_order, is_active)
VALUES
    (91001, 'Karen Lopez',   'Family member', 'Rachel has been a lifesaver for my parents - punctual, kind, and communicates every visit.', 'https://images.eldersphere.app/avatars/karen.jpg', 5, 1, TRUE),
    (91002, 'Fatima Rahman', 'Family member', 'Booking through ElderSphere was so easy, and the real-time messaging keeps us all in the loop.', 'https://images.eldersphere.app/avatars/fatima.jpg', 5, 2, TRUE),
    (91003, 'Oliver Brooks', 'Family member', 'The emergency alert feature gave our whole family real peace of mind.', 'https://images.eldersphere.app/avatars/oliver.jpg', 5, 3, TRUE),
    (91004, 'Elena Petrova', 'Caretaker', 'ElderSphere makes it simple to manage my schedule and connect with the families I care for.', 'https://images.eldersphere.app/avatars/elena.jpg', 5, 4, TRUE);

-- ------------------------------------------------------------
-- Advance sequences so subsequent application inserts don't
-- collide with these explicit IDs.
-- ------------------------------------------------------------
SELECT setval('users_id_seq', (SELECT GREATEST(MAX(id), 1) FROM users));
SELECT setval('caretaker_profiles_id_seq', (SELECT GREATEST(MAX(id), 1) FROM caretaker_profiles));
SELECT setval('caretaker_availability_id_seq', (SELECT GREATEST(MAX(id), 1) FROM caretaker_availability));
SELECT setval('elder_profiles_id_seq', (SELECT GREATEST(MAX(id), 1) FROM elder_profiles));
SELECT setval('service_offerings_id_seq', (SELECT GREATEST(MAX(id), 1) FROM service_offerings));
SELECT setval('bookings_id_seq', (SELECT GREATEST(MAX(id), 1) FROM bookings));
SELECT setval('reviews_id_seq', (SELECT GREATEST(MAX(id), 1) FROM reviews));
SELECT setval('review_replies_id_seq', (SELECT GREATEST(MAX(id), 1) FROM review_replies));
SELECT setval('notifications_id_seq', (SELECT GREATEST(MAX(id), 1) FROM notifications));
SELECT setval('landing_page_config_id_seq', (SELECT GREATEST(MAX(id), 1) FROM landing_page_config));
SELECT setval('landing_features_id_seq', (SELECT GREATEST(MAX(id), 1) FROM landing_features));
SELECT setval('landing_faqs_id_seq', (SELECT GREATEST(MAX(id), 1) FROM landing_faqs));
SELECT setval('landing_testimonials_id_seq', (SELECT GREATEST(MAX(id), 1) FROM landing_testimonials));
