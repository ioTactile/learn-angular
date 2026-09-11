package com.learn.api.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface HabitJpaRepository extends JpaRepository<HabitJpaEntity, UUID> {

	List<HabitJpaEntity> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

	Page<HabitJpaEntity> findByOwnerId(UUID ownerId, Pageable pageable);

	Page<HabitJpaEntity> findByOwnerIdAndTitleContainingIgnoreCase(UUID ownerId, String title, Pageable pageable);

	Page<HabitJpaEntity> findByWorkspaceIdAndOwnerId(UUID workspaceId, UUID ownerId, Pageable pageable);

	Page<HabitJpaEntity> findByWorkspaceIdAndOwnerIdAndTitleContainingIgnoreCase(
			UUID workspaceId,
			UUID ownerId,
			String title,
			Pageable pageable
	);

	Optional<HabitJpaEntity> findByIdAndOwnerId(UUID id, UUID ownerId);
}
