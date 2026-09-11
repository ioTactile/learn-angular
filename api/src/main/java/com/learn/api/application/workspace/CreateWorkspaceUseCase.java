package com.learn.api.application.workspace;

import com.learn.api.domain.workspace.Workspace;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateWorkspaceUseCase {

	private final WorkspaceRepository workspaces;

	public CreateWorkspaceUseCase(WorkspaceRepository workspaces) {
		this.workspaces = workspaces;
	}

	@Transactional
	public Workspace execute(UUID ownerId, String name) {
		return workspaces.save(Workspace.create(ownerId, name));
	}
}
