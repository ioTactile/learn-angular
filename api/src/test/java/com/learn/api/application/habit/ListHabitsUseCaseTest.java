package com.learn.api.application.habit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.learn.api.domain.habit.Habit;
import java.util.List;
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

	ListHabitsUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ListHabitsUseCase(habits);
	}

	@Test
	@DisplayName("liste uniquement les habits du owner")
	void execute_returnsOwnerHabits() {
		UUID ownerId = UUID.randomUUID();
		List<Habit> expected = List.of(Habit.create(ownerId, "Run"));
		when(habits.findAllByOwnerId(ownerId)).thenReturn(expected);

		List<Habit> result = useCase.execute(ownerId);

		assertThat(result).isEqualTo(expected);
	}
}
