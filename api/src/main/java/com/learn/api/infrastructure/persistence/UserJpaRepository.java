package com.learn.api.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

	boolean existsByEmailIgnoreCase(String email);

	Optional<UserJpaEntity> findByEmailIgnoreCase(String email);
}
