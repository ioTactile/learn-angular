package com.learn.api.api.habit;

import java.time.Instant;
import java.time.LocalDate;

public record HabitCompletionResponse(
		String id,
		String habitId,
		LocalDate completedOn,
		String note,
		Instant createdAt
) {
}
