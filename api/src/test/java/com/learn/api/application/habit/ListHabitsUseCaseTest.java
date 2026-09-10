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
	@DisplayName("délègue la pagination/filtre au repository")
	void execute_returnsOwnerHabitsPage() {
		UUID ownerId = UUID.randomUUID();
		HabitPage expected = new HabitPage(List.of(Habit.create(ownerId, "Run")), 0, 10, 1, 1);
		when(habits.findPageByOwnerId(ownerId, "run", 0, 10)).thenReturn(expected);

		HabitPage result = useCase.execute(new ListHabitsQuery(ownerId, " run ", 0, 10));

		assertThat(result).isEqualTo(expected);
	}
}
