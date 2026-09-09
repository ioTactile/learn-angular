package com.learn.api.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Mapping JPA (= table users). Séparé du record de domaine.
 */
@Entity
@Table(name = "users")
class UserJpaEntity {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 320)
	private String email;

	@Column(name = "password_hash", nullable = false, length = 100)
	private String passwordHash;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected UserJpaEntity() {
		// JPA
	}

	UserJpaEntity(UUID id, String email, String passwordHash, Instant createdAt) {
		this.id = id;
		this.email = email;
		this.passwordHash = passwordHash;
		this.createdAt = createdAt;
	}

	UUID getId() {
		return id;
	}

	String getEmail() {
		return email;
	}

	String getPasswordHash() {
		return passwordHash;
	}

	Instant getCreatedAt() {
		return createdAt;
	}
}
