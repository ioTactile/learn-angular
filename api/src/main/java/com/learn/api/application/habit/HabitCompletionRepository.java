package com.learn.api.application.habit;

import com.learn.api.domain.habit.HabitCompletion;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HabitCompletionRepository {

	HabitCompletion save(HabitCompletion completion);

	Optional<HabitCompletion> findByHabitIdAndCompletedOn(UUID habitId, LocalDate completedOn);

	List<HabitCompletion> findByHabitIdAndCompletedOnBetween(
			UUID habitId,
			LocalDate from,
			LocalDate to
	);
}
