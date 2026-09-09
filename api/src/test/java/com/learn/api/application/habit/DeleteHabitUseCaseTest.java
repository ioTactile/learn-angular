package com.learn.api.application.habit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.learn.api.domain.habit.Habit;
import com.learn.api.domain.habit.HabitNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteHabitUseCaseTest {

	@Mock
	HabitRepository habits;

	DeleteHabitUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new DeleteHabitUseCase(habits);
	}

	@Test
	@DisplayName("supprime un habit appartenant au owner")
	void execute_deletesOwnedHabit() {
		UUID ownerId = UUID.randomUUID();
		Habit habit = Habit.create(ownerId, "Run");
		when(habits.findByIdAndOwnerId(habit.id(), ownerId)).thenReturn(Optional.of(habit));

		useCase.execute(habit.id(), ownerId);

		verify(habits).delete(habit);
	}

	@Test
	@DisplayName("habit introuvable ou d'un autre → HabitNotFoundException")
	void execute_whenMissing_throwsNotFound() {
		UUID habitId = UUID.randomUUID();
		UUID ownerId = UUID.randomUUID();
		when(habits.findByIdAndOwnerId(habitId, ownerId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute(habitId, ownerId))
				.isInstanceOf(HabitNotFoundException.class);

		verify(habits, never()).delete(org.mockito.ArgumentMatchers.any());
	}
}
