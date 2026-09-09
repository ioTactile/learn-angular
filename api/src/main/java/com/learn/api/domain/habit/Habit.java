package com.learn.api.domain.habit;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Habit = domaine métier pur.
 * ownerId garantit l'isolation des données par utilisateur.
 * streak : jours consécutifs (règles dans {@link #complete(LocalDate)}).
 */
public record Habit(
		UUID id,
		UUID ownerId,
		String title,
		Instant createdAt,
		int streak,
		LocalDate lastCompletedOn
) {
	public static Habit create(UUID ownerId, String title) {
		String normalized = title.trim();
		if (normalized.isEmpty()) {
			throw new IllegalArgumentException("Habit title must not be blank");
		}
		return new Habit(UUID.randomUUID(), ownerId, normalized, Instant.now(), 0, null);
	}

	/**
	 * Règles streak :
	 * - même jour → inchangé (idempotent)
	 * - jour suivant → streak + 1
	 * - gap / première fois → streak = 1
	 */
	public Habit complete(LocalDate today) {
		if (lastCompletedOn != null && lastCompletedOn.equals(today)) {
			return this;
		}

		int nextStreak = (lastCompletedOn != null && lastCompletedOn.plusDays(1).equals(today))
				? streak + 1
				: 1;

		return new Habit(id, ownerId, title, createdAt, nextStreak, today);
	}
}
