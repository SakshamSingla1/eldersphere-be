-- ============================================================
-- In-app messaging: a conversation between two users, optionally
-- tied to a booking, and the messages exchanged within it.
-- ============================================================
CREATE TABLE conversations (
    id              BIGSERIAL    PRIMARY KEY,
    user_a_id       BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    user_b_id       BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    booking_id      BIGINT       REFERENCES bookings(id) ON DELETE SET NULL,
    last_message_at TIMESTAMP(6),

    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      BIGINT,
    updated_by      BIGINT,

    CONSTRAINT chk_conversation_distinct_users CHECK (user_a_id <> user_b_id)
);

CREATE INDEX idx_conversations_user_a_id ON conversations(user_a_id);
CREATE INDEX idx_conversations_user_b_id ON conversations(user_b_id);
CREATE INDEX idx_conversations_booking_id ON conversations(booking_id);

CREATE TABLE messages (
    id              BIGSERIAL    PRIMARY KEY,
    conversation_id BIGINT       NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_id       BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content         TEXT         NOT NULL,
    sent_at         TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at         TIMESTAMP(6),

    created_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      BIGINT,
    updated_by      BIGINT
);

CREATE INDEX idx_messages_conversation_id_sent_at ON messages(conversation_id, sent_at);
