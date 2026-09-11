package com.learn.api.application.habit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.learn.api.application.workspace.WorkspaceRepository;
import com.learn.api.domain.habit.Habit;
import com.learn.api.domain.workspace.Workspace;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListHabitsUseCaseTest {

	@Mock
	HabitRepository habits;

	@Mock
	WorkspaceRepository workspaces;

	ListHabitsUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ListHabitsUseCase(habits, workspaces);
	}

	@Test
	@DisplayName("délègue la pagination/filtre au repository du workspace")
	void execute_returnsWorkspaceHabitsPage() {
		UUID ownerId = UUID.randomUUID();
		UUID workspaceId = UUID.randomUUID();
		when(workspaces.findByIdAndOwnerId(workspaceId, ownerId))
				.thenReturn(Optional.of(Workspace.create(ownerId, "Perso")));
		HabitPage expected = new HabitPage(
				List.of(Habit.create(ownerId, workspaceId, "Run")),
				0,
				10,
				1,
				1
		);
		when(habits.findPageByWorkspaceIdAndOwnerId(workspaceId, ownerId, "run", 0, 10))
				.thenReturn(expected);

		HabitPage result = useCase.execute(new ListHabitsQuery(ownerId, workspaceId, " run ", 0, 10));

		assertThat(result).isEqualTo(expected);
	}
}
