package com.learn.api.application.habit;

import com.learn.api.domain.habit.Habit;
import java.util.List;

public record HabitPage(
		List<Habit> content,
		int page,
		int size,
		long totalElements,
		int totalPages
) {
}
