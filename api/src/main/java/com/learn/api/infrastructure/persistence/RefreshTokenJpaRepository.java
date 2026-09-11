package com.learn.api.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {

	Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);

	void deleteAllByUserId(UUID userId);
}
