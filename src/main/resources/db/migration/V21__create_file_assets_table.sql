-- ============================================================
-- File assets: metadata for files stored on local disk under uploads/
-- ============================================================
CREATE TABLE file_assets (
    id             BIGSERIAL    PRIMARY KEY,
    url            VARCHAR(1000),
    path           VARCHAR(1000),
    file_name      VARCHAR(500),
    file_type      VARCHAR(255),
    resource_type  VARCHAR(50),
    uploaded_by    BIGINT REFERENCES users(id),

    created_at     TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by     BIGINT,
    updated_by     BIGINT
);
