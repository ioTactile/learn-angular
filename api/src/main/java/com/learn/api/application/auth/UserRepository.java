package com.learn.api.application.auth;

import com.learn.api.domain.user.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port sortant (hexagone) : la couche application ne connaît pas JPA.
 */
public interface UserRepository {

	boolean existsByEmail(String email);

	User save(User user);

	Optional<User> findByEmail(String email);

	Optional<User> findById(UUID id);

	List<User> findAll();

	void incrementTokenVersion(UUID userId);
}
