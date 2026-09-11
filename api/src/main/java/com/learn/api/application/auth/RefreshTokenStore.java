package com.learn.api.application.auth;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenStore {

	void save(UUID userId, String tokenHash, Instant expiresAt);

	Optional<StoredRefreshToken> findByHash(String tokenHash);

	void revokeByHash(String tokenHash, Instant now);

	void deleteAllByUserId(UUID userId);

	record StoredRefreshToken(
			UUID id,
			UUID userId,
			String tokenHash,
			Instant expiresAt,
			Instant revokedAt
	) {
		boolean revoked() {
			return revokedAt != null;
		}

		boolean expired(Instant now) {
			return !expiresAt.isAfter(now);
		}
	}
}
