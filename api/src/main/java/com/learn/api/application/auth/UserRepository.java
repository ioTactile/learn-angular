package com.learn.api.application.auth;

import com.learn.api.domain.user.User;
import java.util.Optional;
import java.util.UUID;

/**
 * Port sortant (hexagone) : la couche application ne connaît pas JPA.
 * Comme une interface repository injectée dans un service Nest/Fastify.
 */
public interface UserRepository {

	boolean existsByEmail(String email);

	User save(User user);

	Optional<User> findByEmail(String email);

	Optional<User> findById(UUID id);
}
