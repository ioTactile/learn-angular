package com.learn.api.infrastructure.persistence;

import com.learn.api.application.workspace.WorkspaceRepository;
import com.learn.api.domain.workspace.Workspace;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class JpaWorkspaceRepository implements WorkspaceRepository {

	private final WorkspaceJpaRepository jpa;

	JpaWorkspaceRepository(WorkspaceJpaRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public Workspace save(Workspace workspace) {
		WorkspaceJpaEntity saved = jpa.save(new WorkspaceJpaEntity(
				workspace.id(),
				workspace.ownerId(),
				workspace.name(),
				workspace.createdAt()
		));
		return toDomain(saved);
	}

	@Override
	public List<Workspace> findAllByOwnerId(UUID ownerId) {
		return jpa.findAllByOwnerIdOrderByCreatedAtAsc(ownerId).stream().map(this::toDomain).toList();
	}

	@Override
	public Optional<Workspace> findByIdAndOwnerId(UUID id, UUID ownerId) {
		return jpa.findByIdAndOwnerId(id, ownerId).map(this::toDomain);
	}

	private Workspace toDomain(WorkspaceJpaEntity entity) {
		return new Workspace(entity.getId(), entity.getOwnerId(), entity.getName(), entity.getCreatedAt());
	}
}
