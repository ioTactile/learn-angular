package com.learn.api.application.habit;

import com.learn.api.domain.habit.Habit;
import com.learn.api.domain.habit.HabitNotFoundException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompleteHabitUseCase {

	private final HabitRepository habits;
	private final Clock clock;

	public CompleteHabitUseCase(HabitRepository habits, Clock clock) {
		this.habits = habits;
		this.clock = clock;
	}

	@Transactional
	public Habit execute(UUID habitId, UUID ownerId) {
		Habit habit = habits.findByIdAndOwnerId(habitId, ownerId)
				.orElseThrow(() -> new HabitNotFoundException(habitId));

		LocalDate today = LocalDate.now(clock);
		Habit completed = habit.complete(today);
		return habits.save(completed);
	}
}
