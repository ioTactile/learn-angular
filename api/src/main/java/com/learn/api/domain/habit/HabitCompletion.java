package com.learn.api.domain.habit;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record HabitCompletion(
		UUID id,
		UUID habitId,
		LocalDate completedOn,
		String note,
		Instant createdAt
) {
	public static HabitCompletion create(UUID habitId, LocalDate completedOn, String note) {
		String normalized = note == null || note.isBlank() ? null : note.trim();
		return new HabitCompletion(UUID.randomUUID(), habitId, completedOn, normalized, Instant.now());
	}

	public HabitCompletion withNote(String note) {
		String normalized = note == null || note.isBlank() ? null : note.trim();
		return new HabitCompletion(id, habitId, completedOn, normalized, createdAt);
	}
}
