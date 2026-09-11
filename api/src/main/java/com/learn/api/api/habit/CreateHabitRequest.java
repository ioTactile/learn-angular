package com.learn.api.api.habit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateHabitRequest(
		@NotNull UUID workspaceId,
		@NotBlank @Size(max = 120) String title
) {
}
