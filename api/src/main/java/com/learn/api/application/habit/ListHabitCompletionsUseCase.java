package com.learn.api.application.habit;

import com.learn.api.domain.habit.HabitCompletion;
import com.learn.api.domain.habit.HabitNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListHabitCompletionsUseCase {

	private final HabitRepository habits;
	private final HabitCompletionRepository completions;

	public ListHabitCompletionsUseCase(HabitRepository habits, HabitCompletionRepository completions) {
		this.habits = habits;
		this.completions = completions;
	}

	@Transactional(readOnly = true)
	public List<HabitCompletion> execute(UUID habitId, UUID ownerId, LocalDate from, LocalDate to) {
		habits.findByIdAndOwnerId(habitId, ownerId)
				.orElseThrow(() -> new HabitNotFoundException(habitId));

		LocalDate effectiveTo = to != null ? to : LocalDate.now();
		LocalDate effectiveFrom = from != null ? from : effectiveTo.minusDays(30);
		if (effectiveFrom.isAfter(effectiveTo)) {
			LocalDate swap = effectiveFrom;
			effectiveFrom = effectiveTo;
			effectiveTo = swap;
		}

		return completions.findByHabitIdAndCompletedOnBetween(habitId, effectiveFrom, effectiveTo);
	}
}
