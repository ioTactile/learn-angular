-- Espaces de travail : une liste d'habitudes appartient à un workspace
CREATE TABLE workspaces (
    id          UUID PRIMARY KEY,
    owner_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name        VARCHAR(120) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_workspaces_owner_id ON workspaces(owner_id);

ALTER TABLE habits
    ADD COLUMN workspace_id UUID REFERENCES workspaces(id) ON DELETE CASCADE;

-- Backfill : un workspace "Perso" par propriétaire d'habitudes existantes
INSERT INTO workspaces (id, owner_id, name, created_at)
SELECT gen_random_uuid(), owner_id, 'Perso', NOW()
FROM (SELECT DISTINCT owner_id FROM habits) owners;

UPDATE habits h
SET workspace_id = w.id
FROM workspaces w
WHERE h.owner_id = w.owner_id
  AND h.workspace_id IS NULL
  AND w.name = 'Perso';

ALTER TABLE habits
    ALTER COLUMN workspace_id SET NOT NULL;

CREATE INDEX idx_habits_workspace_id ON habits(workspace_id);
