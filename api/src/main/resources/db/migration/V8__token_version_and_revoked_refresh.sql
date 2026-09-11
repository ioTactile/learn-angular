-- Version de session (invalide tous les access JWT) + révocation des refresh
ALTER TABLE users
    ADD COLUMN token_version INTEGER NOT NULL DEFAULT 0;

ALTER TABLE refresh_tokens
    ADD COLUMN revoked_at TIMESTAMPTZ;
