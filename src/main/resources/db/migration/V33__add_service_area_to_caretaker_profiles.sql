-- ============================================================
-- Advanced caretaker search: a free-text service area/location
-- field to filter on, alongside the existing category/rating/
-- verification filters. Nullable - caretakers can set it later
-- via their profile. Backfilled below for existing seeded
-- caretakers so the filter has real data to match against.
-- ============================================================
ALTER TABLE caretaker_profiles ADD COLUMN service_area VARCHAR(255);

UPDATE caretaker_profiles SET service_area = 'Springfield' WHERE id = 90001;

UPDATE caretaker_profiles SET service_area = 'Riverton'  WHERE id IN (91001, 91004, 91008);
UPDATE caretaker_profiles SET service_area = 'Millbrook'  WHERE id IN (91002, 91005, 91009);
UPDATE caretaker_profiles SET service_area = 'Fairview'   WHERE id IN (91003, 91006);
UPDATE caretaker_profiles SET service_area = 'Springfield' WHERE id = 91007;
