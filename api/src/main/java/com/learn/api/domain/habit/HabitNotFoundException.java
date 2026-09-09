package com.learn.api.domain.habit;

import java.util.UUID;

/**
 * Habit introuvable pour ce user.
 * On renvoie la même erreur si l'id n'existe pas OU appartient à un autre
 * (évite de fuiter l'existence d'une ressource).
 */
public class HabitNotFoundException extends RuntimeException {

	public HabitNotFoundException(UUID habitId) {
		super("Habit not found: " + habitId);
	}
}
