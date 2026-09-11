package com.learn.api.api.habit;

import java.time.Instant;
import java.time.LocalDate;

public record HabitResponse(
		String id,
		String workspaceId,
		String title,
		Instant createdAt,
		int streak,
		LocalDate lastCompletedOn
) {
}
