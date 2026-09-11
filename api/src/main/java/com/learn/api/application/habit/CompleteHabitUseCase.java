package com.learn.api.application.habit;

import com.learn.api.domain.habit.Habit;
import com.learn.api.domain.habit.HabitCompletion;
import com.learn.api.domain.habit.HabitNotFoundException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompleteHabitUseCase {

	private final HabitRepository habits;
	private final HabitCompletionRepository completions;
	private final Clock clock;

	public CompleteHabitUseCase(
			HabitRepository habits,
			HabitCompletionRepository completions,
			Clock clock
	) {
		this.habits = habits;
		this.completions = completions;
		this.clock = clock;
	}

	@Transactional
	public Habit execute(UUID habitId, UUID ownerId, String note) {
		Habit habit = habits.findByIdAndOwnerId(habitId, ownerId)
				.orElseThrow(() -> new HabitNotFoundException(habitId));

		LocalDate today = LocalDate.now(clock);
		Habit completed = habits.save(habit.complete(today));

		completions.findByHabitIdAndCompletedOn(habitId, today)
				.map(existing -> completions.save(existing.withNote(note)))
				.orElseGet(() -> completions.save(HabitCompletion.create(habitId, today, note)));

		return completed;
	}
}
