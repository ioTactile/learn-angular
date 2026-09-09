-- Users : table minimale pour l'auth (register / login)
CREATE TABLE users (
    id              UUID PRIMARY KEY,
    email           VARCHAR(320) NOT NULL,
    password_hash   VARCHAR(100) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_email UNIQUE (email)
);
