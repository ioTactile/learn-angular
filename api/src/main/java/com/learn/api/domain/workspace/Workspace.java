package com.learn.api.domain.workspace;

import java.time.Instant;
import java.util.UUID;

public record Workspace(
		UUID id,
		UUID ownerId,
		String name,
		Instant createdAt
) {
	public static Workspace create(UUID ownerId, String name) {
		String normalized = name.trim();
		if (normalized.isEmpty()) {
			throw new IllegalArgumentException("Workspace name must not be blank");
		}
		return new Workspace(UUID.randomUUID(), ownerId, normalized, Instant.now());
	}

	public static Workspace defaultPersonal(UUID ownerId) {
		return create(ownerId, "Perso");
	}
}
