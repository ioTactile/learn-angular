-- Streak : jours consécutifs de completion
ALTER TABLE habits
    ADD COLUMN streak INT NOT NULL DEFAULT 0,
    ADD COLUMN last_completed_on DATE;
