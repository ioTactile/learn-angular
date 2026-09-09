-- Habits appartenant à un user (isolation multi-tenant simple)
CREATE TABLE habits (
    id          UUID PRIMARY KEY,
    owner_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title       VARCHAR(120) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_habits_owner_id ON habits(owner_id);
