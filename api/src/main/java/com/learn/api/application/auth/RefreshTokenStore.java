package com.learn.api.application.auth;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenStore {

	void save(UUID userId, String tokenHash, Instant expiresAt);

	Optional<StoredRefreshToken> findValidByHash(String tokenHash, Instant now);

	void deleteByHash(String tokenHash);

	void deleteAllByUserId(UUID userId);

	record StoredRefreshToken(UUID id, UUID userId, String tokenHash, Instant expiresAt) {
	}
}
