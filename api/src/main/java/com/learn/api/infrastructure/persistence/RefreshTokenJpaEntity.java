package com.learn.api.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenJpaEntity {

	@Id
	private UUID id;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Column(name = "token_hash", nullable = false, length = 64, unique = true)
	private String tokenHash;

	@Column(name = "expires_at", nullable = false)
	private Instant expiresAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected RefreshTokenJpaEntity() {
	}

	RefreshTokenJpaEntity(UUID id, UUID userId, String tokenHash, Instant expiresAt, Instant createdAt) {
		this.id = id;
		this.userId = userId;
		this.tokenHash = tokenHash;
		this.expiresAt = expiresAt;
		this.createdAt = createdAt;
	}

	UUID getId() {
		return id;
	}

	UUID getUserId() {
		return userId;
	}

	String getTokenHash() {
		return tokenHash;
	}

	Instant getExpiresAt() {
		return expiresAt;
	}
}
