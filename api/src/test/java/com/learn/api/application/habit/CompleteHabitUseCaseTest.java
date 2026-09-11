package com.learn.api.application.habit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.learn.api.domain.habit.Habit;
import com.learn.api.domain.habit.HabitCompletion;
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

	@Mock
	HabitCompletionRepository completions;

	Clock clock;
	CompleteHabitUseCase useCase;

	@BeforeEach
	void setUp() {
		clock = Clock.fixed(Instant.parse("2026-09-09T12:00:00Z"), ZoneOffset.UTC);
		useCase = new CompleteHabitUseCase(habits, completions, clock);
	}

	@Test
	@DisplayName("complete un habit du owner, persiste streak + journal")
	void execute_completesOwnedHabit() {
		UUID ownerId = UUID.randomUUID();
		Habit habit = Habit.create(ownerId, UUID.randomUUID(), "Run");
		when(habits.findByIdAndOwnerId(habit.id(), ownerId)).thenReturn(Optional.of(habit));
		when(habits.save(any(Habit.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(completions.findByHabitIdAndCompletedOn(habit.id(), LocalDate.of(2026, 9, 9)))
				.thenReturn(Optional.empty());
		when(completions.save(any(HabitCompletion.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Habit result = useCase.execute(habit.id(), ownerId, "Felt good");

		assertThat(result.streak()).isEqualTo(1);
		assertThat(result.lastCompletedOn()).isEqualTo(LocalDate.of(2026, 9, 9));

		ArgumentCaptor<HabitCompletion> completionCaptor = ArgumentCaptor.forClass(HabitCompletion.class);
		verify(completions).save(completionCaptor.capture());
		assertThat(completionCaptor.getValue().note()).isEqualTo("Felt good");
	}

	@Test
	@DisplayName("habit d'un autre → HabitNotFoundException")
	void execute_whenNotOwned_throwsNotFound() {
		UUID habitId = UUID.randomUUID();
		UUID ownerId = UUID.randomUUID();
		when(habits.findByIdAndOwnerId(habitId, ownerId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute(habitId, ownerId, null))
				.isInstanceOf(HabitNotFoundException.class);
	}
}
