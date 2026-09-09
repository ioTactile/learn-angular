package com.learn.api.api.habit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateHabitRequest(
		@NotBlank @Size(max = 120) String title
) {
}
