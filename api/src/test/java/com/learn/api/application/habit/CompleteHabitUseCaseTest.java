package com.learn.api.application.habit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.learn.api.domain.habit.Habit;
import com.learn.api.domain.habit.HabitNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
class CompleteHabitUseCaseTest {

	@Mock
	HabitRepository habits;

	Clock clock;
	CompleteHabitUseCase useCase;

	@BeforeEach
	void setUp() {
		clock = Clock.fixed(Instant.parse("2026-09-09T12:00:00Z"), ZoneOffset.UTC);
		useCase = new CompleteHabitUseCase(habits, clock);
	}

	@Test
	@DisplayName("complete un habit du owner et persiste le streak")
	void execute_completesOwnedHabit() {
		UUID ownerId = UUID.randomUUID();
		Habit habit = Habit.create(ownerId, "Run");
		when(habits.findByIdAndOwnerId(habit.id(), ownerId)).thenReturn(Optional.of(habit));
		when(habits.save(any(Habit.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Habit result = useCase.execute(habit.id(), ownerId);

		assertThat(result.streak()).isEqualTo(1);
		assertThat(result.lastCompletedOn()).isEqualTo(LocalDate.of(2026, 9, 9));

		ArgumentCaptor<Habit> captor = ArgumentCaptor.forClass(Habit.class);
		verify(habits).save(captor.capture());
		assertThat(captor.getValue().streak()).isEqualTo(1);
	}

	@Test
	@DisplayName("habit d'un autre → HabitNotFoundException")
	void execute_whenNotOwned_throwsNotFound() {
		UUID habitId = UUID.randomUUID();
		UUID ownerId = UUID.randomUUID();
		when(habits.findByIdAndOwnerId(habitId, ownerId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute(habitId, ownerId))
				.isInstanceOf(HabitNotFoundException.class);
	}
}
