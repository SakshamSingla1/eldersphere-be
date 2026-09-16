ALTER TABLE messages
    ADD COLUMN file_asset_id BIGINT REFERENCES file_assets(id) ON DELETE SET NULL,
    ADD COLUMN file_url VARCHAR(500);

ALTER TABLE messages ALTER COLUMN content DROP NOT NULL;

INSERT INTO nav_links (user_type, nav_group, nav_index, name, path, icon, required_permission, super_admin_only) VALUES
    ('ADMIN', 'Care Operations', 16, 'Messages', '/admin/messages', 'ChatBubbleOutlineIcon', NULL, FALSE);
