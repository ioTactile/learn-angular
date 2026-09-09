package com.learn.api.domain.user;

import java.time.Instant;
import java.util.UUID;

/**
 * Entité de domaine (pas d'annotations JPA ici).
 * En React/Node tu aurais un type User "métier" séparé du modèle Prisma/Drizzle.
 */
public record User(
		UUID id,
		String email,
		String passwordHash,
		Instant createdAt
) {
	public static User create(String email, String passwordHash) {
		return new User(UUID.randomUUID(), email.trim().toLowerCase(), passwordHash, Instant.now());
	}
}
