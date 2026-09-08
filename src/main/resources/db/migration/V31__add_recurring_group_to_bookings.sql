-- ============================================================
-- Recurring bookings: a booking created as part of a weekly
-- series shares a recurring_group_id (UUID string) with its
-- siblings, so the whole series can be listed/cancelled together.
-- NULL for one-off bookings (all existing rows).
-- ============================================================
ALTER TABLE bookings ADD COLUMN recurring_group_id VARCHAR(36);

CREATE INDEX idx_bookings_recurring_group_id ON bookings(recurring_group_id);
