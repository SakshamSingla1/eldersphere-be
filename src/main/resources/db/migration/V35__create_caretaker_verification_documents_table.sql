-- ============================================================
-- Caretaker verification documents: evidence a caretaker submits
-- (e.g. ID, certification, background-check PDF) backing their
-- CaretakerProfile.verificationStatus review. Reuses the existing
-- file_assets upload infrastructure (ResourceTypeEnum.CARETAKER_
-- VERIFICATION_DOCUMENT) - this table is just the join between a
-- caretaker profile and the file(s) they submitted, so an admin
-- reviewing a profile can list everything that backs it.
--
-- A caretaker can submit any number of documents over time; nothing
-- here is deleted on its own — an admin removes evidence only by
-- deleting the underlying file_asset (ON DELETE CASCADE below keeps
-- this table consistent with that).
-- ============================================================
CREATE TABLE caretaker_verification_documents (
    id                    BIGSERIAL    PRIMARY KEY,
    caretaker_profile_id  BIGINT       NOT NULL REFERENCES caretaker_profiles(id) ON DELETE CASCADE,
    file_asset_id         BIGINT       NOT NULL REFERENCES file_assets(id) ON DELETE CASCADE,
    uploaded_by           BIGINT       REFERENCES users(id) ON DELETE SET NULL,

    created_at            TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by            BIGINT,
    updated_by            BIGINT
);

CREATE INDEX idx_caretaker_verification_documents_caretaker_profile_id
    ON caretaker_verification_documents(caretaker_profile_id);
