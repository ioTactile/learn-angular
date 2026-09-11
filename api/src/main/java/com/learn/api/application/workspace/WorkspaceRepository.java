package com.learn.api.application.workspace;

import com.learn.api.domain.workspace.Workspace;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceRepository {

	Workspace save(Workspace workspace);

	List<Workspace> findAllByOwnerId(UUID ownerId);

	Optional<Workspace> findByIdAndOwnerId(UUID id, UUID ownerId);
}
