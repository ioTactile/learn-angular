package com.learn.api.application.workspace;

import com.learn.api.domain.workspace.Workspace;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListWorkspacesUseCase {

	private final WorkspaceRepository workspaces;

	public ListWorkspacesUseCase(WorkspaceRepository workspaces) {
		this.workspaces = workspaces;
	}

	@Transactional(readOnly = true)
	public List<Workspace> execute(UUID ownerId) {
		return workspaces.findAllByOwnerId(ownerId);
	}
}
