package com.learn.api.application.habit;

import com.learn.api.application.workspace.WorkspaceRepository;
import com.learn.api.domain.habit.Habit;
import com.learn.api.domain.workspace.WorkspaceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateHabitUseCase {

	private final HabitRepository habits;
	private final WorkspaceRepository workspaces;

	public CreateHabitUseCase(HabitRepository habits, WorkspaceRepository workspaces) {
		this.habits = habits;
		this.workspaces = workspaces;
	}

	@Transactional
	public Habit execute(CreateHabitCommand command) {
		workspaces.findByIdAndOwnerId(command.workspaceId(), command.ownerId())
				.orElseThrow(() -> new WorkspaceNotFoundException(command.workspaceId()));

		Habit habit = Habit.create(command.ownerId(), command.workspaceId(), command.title());
		return habits.save(habit);
	}
}
