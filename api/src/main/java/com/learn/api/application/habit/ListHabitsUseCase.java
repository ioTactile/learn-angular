package com.learn.api.application.habit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListHabitsUseCase {

	private final HabitRepository habits;

	public ListHabitsUseCase(HabitRepository habits) {
		this.habits = habits;
	}

	@Transactional(readOnly = true)
	public HabitPage execute(ListHabitsQuery query) {
		int page = Math.max(query.page(), 0);
		int size = query.size() <= 0 ? 10 : Math.min(query.size(), 100);
		String q = query.q() == null ? "" : query.q().trim();
		return habits.findPageByOwnerId(query.ownerId(), q, page, size);
	}
}
