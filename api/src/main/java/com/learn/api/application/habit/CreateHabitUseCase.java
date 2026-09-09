package com.learn.api.application.habit;

import com.learn.api.domain.habit.Habit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateHabitUseCase {

	private final HabitRepository habits;

	public CreateHabitUseCase(HabitRepository habits) {
		this.habits = habits;
	}

	@Transactional
	public Habit execute(CreateHabitCommand command) {
		Habit habit = Habit.create(command.ownerId(), command.title());
		return habits.save(habit);
	}
}
