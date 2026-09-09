package com.learn.api.application.habit;

import com.learn.api.domain.habit.Habit;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListHabitsUseCase {

	private final HabitRepository habits;

	public ListHabitsUseCase(HabitRepository habits) {
		this.habits = habits;
	}

	@Transactional(readOnly = true)
	public List<Habit> execute(UUID ownerId) {
		return habits.findAllByOwnerId(ownerId);
	}
}
