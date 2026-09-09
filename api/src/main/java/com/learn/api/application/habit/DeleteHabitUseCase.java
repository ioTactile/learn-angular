package com.learn.api.application.habit;

import com.learn.api.domain.habit.HabitNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteHabitUseCase {

	private final HabitRepository habits;

	public DeleteHabitUseCase(HabitRepository habits) {
		this.habits = habits;
	}

	@Transactional
	public void execute(UUID habitId, UUID ownerId) {
		var habit = habits.findByIdAndOwnerId(habitId, ownerId)
				.orElseThrow(() -> new HabitNotFoundException(habitId));
		habits.delete(habit);
	}
}
