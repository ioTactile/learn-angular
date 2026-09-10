package com.learn.api.api.habit;

import java.util.List;

public record HabitPageResponse(
		List<HabitResponse> content,
		int page,
		int size,
		long totalElements,
		int totalPages
) {
}
