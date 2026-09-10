package com.learn.api.infrastructure.persistence;

import com.learn.api.application.auth.RefreshTokenStore;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class JpaRefreshTokenStore implements RefreshTokenStore {

	private final RefreshTokenJpaRepository jpa;

	JpaRefreshTokenStore(RefreshTokenJpaRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public void save(UUID userId, String tokenHash, Instant expiresAt) {
		jpa.save(new RefreshTokenJpaEntity(
				UUID.randomUUID(),
				userId,
				tokenHash,
				expiresAt,
				Instant.now()
		));
	}

	@Override
	public Optional<StoredRefreshToken> findValidByHash(String tokenHash, Instant now) {
		return jpa.findByTokenHashAndExpiresAtAfter(tokenHash, now)
				.map(entity -> new StoredRefreshToken(
						entity.getId(),
						entity.getUserId(),
						entity.getTokenHash(),
						entity.getExpiresAt()
				));
	}

	@Override
	public void deleteByHash(String tokenHash) {
		jpa.deleteByTokenHash(tokenHash);
	}

	@Override
	public void deleteAllByUserId(UUID userId) {
		jpa.deleteAllByUserId(userId);
	}
}
