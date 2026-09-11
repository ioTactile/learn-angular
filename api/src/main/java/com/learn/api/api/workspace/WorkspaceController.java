package com.learn.api.api.workspace;

import com.learn.api.application.workspace.CreateWorkspaceUseCase;
import com.learn.api.application.workspace.ListWorkspacesUseCase;
import com.learn.api.domain.workspace.Workspace;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {

	private final ListWorkspacesUseCase listWorkspaces;
	private final CreateWorkspaceUseCase createWorkspace;

	public WorkspaceController(
			ListWorkspacesUseCase listWorkspaces,
			CreateWorkspaceUseCase createWorkspace
	) {
		this.listWorkspaces = listWorkspaces;
		this.createWorkspace = createWorkspace;
	}

	@GetMapping
	public List<WorkspaceResponse> list(Authentication authentication) {
		return listWorkspaces.execute(currentUserId(authentication)).stream()
				.map(this::toResponse)
				.toList();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public WorkspaceResponse create(
			@Valid @RequestBody CreateWorkspaceRequest request,
			Authentication authentication
	) {
		Workspace workspace = createWorkspace.execute(currentUserId(authentication), request.name());
		return toResponse(workspace);
	}

	private static UUID currentUserId(Authentication authentication) {
		return UUID.fromString(authentication.getName());
	}

	private WorkspaceResponse toResponse(Workspace workspace) {
		return new WorkspaceResponse(
				workspace.id().toString(),
				workspace.name(),
				workspace.createdAt().toString()
		);
	}
}
