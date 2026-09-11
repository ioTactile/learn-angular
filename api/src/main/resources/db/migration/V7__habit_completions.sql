-- Journal des complétions (historique + note optionnelle)
CREATE TABLE habit_completions (
    id            UUID PRIMARY KEY,
    habit_id      UUID NOT NULL REFERENCES habits(id) ON DELETE CASCADE,
    completed_on  DATE NOT NULL,
    note          VARCHAR(500),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_habit_completion_day UNIQUE (habit_id, completed_on)
);

CREATE INDEX idx_habit_completions_habit_id ON habit_completions(habit_id);
CREATE INDEX idx_habit_completions_completed_on ON habit_completions(completed_on);
