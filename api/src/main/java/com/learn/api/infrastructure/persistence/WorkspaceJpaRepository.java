package com.learn.api.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface WorkspaceJpaRepository extends JpaRepository<WorkspaceJpaEntity, UUID> {

	List<WorkspaceJpaEntity> findAllByOwnerIdOrderByCreatedAtAsc(UUID ownerId);

	Optional<WorkspaceJpaEntity> findByIdAndOwnerId(UUID id, UUID ownerId);
}
