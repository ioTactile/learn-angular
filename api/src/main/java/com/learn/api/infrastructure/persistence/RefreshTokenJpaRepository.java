package com.learn.api.infrastructure.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {

	Optional<RefreshTokenJpaEntity> findByTokenHashAndExpiresAtAfter(String tokenHash, Instant now);

	void deleteByTokenHash(String tokenHash);

	void deleteAllByUserId(UUID userId);
}
