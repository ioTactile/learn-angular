package com.learn.api.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface HabitJpaRepository extends JpaRepository<HabitJpaEntity, UUID> {

	List<HabitJpaEntity> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

	Optional<HabitJpaEntity> findByIdAndOwnerId(UUID id, UUID ownerId);
}
