package com.learn.api.infrastructure.persistence;

import com.learn.api.application.auth.UserRepository;
import com.learn.api.domain.user.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adapter : branche le port UserRepository sur Spring Data JPA.
 */
@Repository
class JpaUserRepository implements UserRepository {

	private final UserJpaRepository jpa;

	JpaUserRepository(UserJpaRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public boolean existsByEmail(String email) {
		return jpa.existsByEmailIgnoreCase(email);
	}

	@Override
	public User save(User user) {
		UserJpaEntity entity = new UserJpaEntity(
				user.id(),
				user.email(),
				user.passwordHash(),
				user.role(),
				user.createdAt(),
				user.tokenVersion()
		);
		UserJpaEntity saved = jpa.save(entity);
		return toDomain(saved);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return jpa.findByEmailIgnoreCase(email).map(this::toDomain);
	}

	@Override
	public Optional<User> findById(UUID id) {
		return jpa.findById(id).map(this::toDomain);
	}

	@Override
	public List<User> findAll() {
		return jpa.findAll().stream().map(this::toDomain).toList();
	}

	@Override
	@Transactional
	public void incrementTokenVersion(UUID userId) {
		jpa.incrementTokenVersion(userId);
	}

	private User toDomain(UserJpaEntity entity) {
		return new User(
				entity.getId(),
				entity.getEmail(),
				entity.getPasswordHash(),
				entity.getRole(),
				entity.getCreatedAt(),
				entity.getTokenVersion()
		);
	}
}
