package com.learn.api.api.habit;

import jakarta.validation.constraints.Size;

public record CompleteHabitRequest(
		@Size(max = 500) String note
) {
}
