package com.learn.api.infrastructure.persistence;

import com.learn.api.application.auth.RefreshTokenStore;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
				Instant.now(),
				null
		));
	}

	@Override
	public Optional<StoredRefreshToken> findByHash(String tokenHash) {
		return jpa.findByTokenHash(tokenHash).map(this::toStored);
	}

	@Override
	@Transactional
	public void revokeByHash(String tokenHash, Instant now) {
		jpa.findByTokenHash(tokenHash).ifPresent(entity -> {
			entity.revoke(now);
			jpa.save(entity);
		});
	}

	@Override
	public void deleteAllByUserId(UUID userId) {
		jpa.deleteAllByUserId(userId);
	}

	private StoredRefreshToken toStored(RefreshTokenJpaEntity entity) {
		return new StoredRefreshToken(
				entity.getId(),
				entity.getUserId(),
				entity.getTokenHash(),
				entity.getExpiresAt(),
				entity.getRevokedAt()
		);
	}
}
