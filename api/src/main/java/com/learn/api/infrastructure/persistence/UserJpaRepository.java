package com.learn.api.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

	boolean existsByEmailIgnoreCase(String email);

	Optional<UserJpaEntity> findByEmailIgnoreCase(String email);

	@Modifying(clearAutomatically = true)
	@Query("update UserJpaEntity u set u.tokenVersion = u.tokenVersion + 1 where u.id = :id")
	int incrementTokenVersion(@Param("id") UUID id);
}
