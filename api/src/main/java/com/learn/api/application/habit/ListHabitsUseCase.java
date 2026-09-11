package com.learn.api.application.habit;

import com.learn.api.application.workspace.WorkspaceRepository;
import com.learn.api.domain.workspace.WorkspaceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListHabitsUseCase {

	private final HabitRepository habits;
	private final WorkspaceRepository workspaces;

	public ListHabitsUseCase(HabitRepository habits, WorkspaceRepository workspaces) {
		this.habits = habits;
		this.workspaces = workspaces;
	}

	@Transactional(readOnly = true)
	public HabitPage execute(ListHabitsQuery query) {
		workspaces.findByIdAndOwnerId(query.workspaceId(), query.ownerId())
				.orElseThrow(() -> new WorkspaceNotFoundException(query.workspaceId()));

		int page = Math.max(query.page(), 0);
		int size = query.size() <= 0 ? 10 : Math.min(query.size(), 100);
		String q = query.q() == null ? "" : query.q().trim();
		return habits.findPageByWorkspaceIdAndOwnerId(
				query.workspaceId(),
				query.ownerId(),
				q,
				page,
				size
		);
	}
}
