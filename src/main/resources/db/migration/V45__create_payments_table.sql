-- ============================================================
-- Payments: one row per Stripe PaymentIntent attempt against a booking.
-- Multiple PENDING/FAILED rows can exist for the same booking (retries),
-- but the partial unique index below guarantees at most one SUCCEEDED
-- row per booking - a booking cannot be paid for twice.
-- ============================================================
CREATE TABLE payments (
    id                        BIGSERIAL      PRIMARY KEY,
    booking_id                BIGINT         NOT NULL REFERENCES bookings(id),
    family_user_id            BIGINT         NOT NULL,
    amount                    NUMERIC(10, 2) NOT NULL,
    currency                  VARCHAR(3)     NOT NULL DEFAULT 'usd',
    status                    VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    stripe_payment_intent_id  VARCHAR(255)   UNIQUE,
    stripe_charge_id          VARCHAR(255),
    failure_reason            TEXT,
    paid_at                   TIMESTAMP(6),
    refunded_at               TIMESTAMP(6),

    created_at                TIMESTAMP(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMP(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                BIGINT,
    updated_by                BIGINT
);

CREATE INDEX idx_payments_booking_id ON payments(booking_id);
CREATE INDEX idx_payments_family_user_id ON payments(family_user_id);
CREATE INDEX idx_payments_status ON payments(status);

-- At most one successful payment per booking; PENDING/FAILED retry rows are unrestricted.
CREATE UNIQUE INDEX ux_payments_booking_succeeded ON payments(booking_id) WHERE status = 'SUCCEEDED';

INSERT INTO permissions (name, description) VALUES
    ('PAYMENTS_VIEW', 'View payments and caretaker earnings'),
    ('PAYMENTS_MANAGE', 'Refund payments')
ON CONFLICT (name) DO NOTHING;

INSERT INTO nav_links (user_type, nav_group, nav_index, name, path, icon, required_permission, super_admin_only) VALUES
    ('ADMIN', 'Care Operations', 16, 'Payments', '/admin/payments', 'PaymentIcon', 'PAYMENTS_VIEW', FALSE);

INSERT INTO nav_links (user_type, nav_index, name, path, icon) VALUES
    ('CARETAKER', 8, 'Earnings', '/caretaker/earnings', 'PaidIcon');
