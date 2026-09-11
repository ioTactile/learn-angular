package com.learn.api.application.habit;

import com.learn.api.domain.habit.Habit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HabitRepository {

	Habit save(Habit habit);

	List<Habit> findAllByOwnerId(UUID ownerId);

	HabitPage findPageByOwnerId(UUID ownerId, String query, int page, int size);

	HabitPage findPageByWorkspaceIdAndOwnerId(
			UUID workspaceId,
			UUID ownerId,
			String query,
			int page,
			int size
	);

	Optional<Habit> findByIdAndOwnerId(UUID id, UUID ownerId);

	void delete(Habit habit);
}
