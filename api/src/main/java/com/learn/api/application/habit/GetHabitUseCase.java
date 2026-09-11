package com.learn.api.application.habit;

import com.learn.api.domain.habit.Habit;
import com.learn.api.domain.habit.HabitNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetHabitUseCase {

	private final HabitRepository habits;

	public GetHabitUseCase(HabitRepository habits) {
		this.habits = habits;
	}

	@Transactional(readOnly = true)
	public Habit execute(UUID habitId, UUID ownerId) {
		return habits.findByIdAndOwnerId(habitId, ownerId)
				.orElseThrow(() -> new HabitNotFoundException(habitId));
	}
}
