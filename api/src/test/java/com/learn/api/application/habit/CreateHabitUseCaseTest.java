package com.learn.api.application.habit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.learn.api.application.workspace.WorkspaceRepository;
import com.learn.api.domain.habit.Habit;
import com.learn.api.domain.workspace.Workspace;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateHabitUseCaseTest {

	@Mock
	HabitRepository habits;

	@Mock
	WorkspaceRepository workspaces;

	CreateHabitUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new CreateHabitUseCase(habits, workspaces);
	}

	@Test
	@DisplayName("crée un habit rattaché au workspace du owner")
	void execute_savesHabitForOwner() {
		UUID ownerId = UUID.randomUUID();
		UUID workspaceId = UUID.randomUUID();
		when(workspaces.findByIdAndOwnerId(workspaceId, ownerId))
				.thenReturn(Optional.of(Workspace.create(ownerId, "Perso")));
		when(habits.save(any(Habit.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Habit created = useCase.execute(new CreateHabitCommand(ownerId, workspaceId, "  Drink water  "));

		ArgumentCaptor<Habit> captor = ArgumentCaptor.forClass(Habit.class);
		verify(habits).save(captor.capture());
		assertThat(captor.getValue().ownerId()).isEqualTo(ownerId);
		assertThat(captor.getValue().workspaceId()).isEqualTo(workspaceId);
		assertThat(captor.getValue().title()).isEqualTo("Drink water");
		assertThat(created.title()).isEqualTo("Drink water");
	}
}
