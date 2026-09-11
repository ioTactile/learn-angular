package com.learn.api.infrastructure.persistence;

import com.learn.api.application.habit.HabitPage;
import com.learn.api.application.habit.HabitRepository;
import com.learn.api.domain.habit.Habit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
class JpaHabitRepository implements HabitRepository {

	private final HabitJpaRepository jpa;

	JpaHabitRepository(HabitJpaRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public Habit save(Habit habit) {
		HabitJpaEntity entity = new HabitJpaEntity(
				habit.id(),
				habit.ownerId(),
				habit.workspaceId(),
				habit.title(),
				habit.createdAt(),
				habit.streak(),
				habit.lastCompletedOn()
		);
		return toDomain(jpa.save(entity));
	}

	@Override
	public List<Habit> findAllByOwnerId(UUID ownerId) {
		return jpa.findAllByOwnerIdOrderByCreatedAtDesc(ownerId).stream()
				.map(this::toDomain)
				.toList();
	}

	@Override
	public HabitPage findPageByOwnerId(UUID ownerId, String query, int page, int size) {
		var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<HabitJpaEntity> result = query.isBlank()
				? jpa.findByOwnerId(ownerId, pageable)
				: jpa.findByOwnerIdAndTitleContainingIgnoreCase(ownerId, query, pageable);
		return toPage(result);
	}

	@Override
	public HabitPage findPageByWorkspaceIdAndOwnerId(
			UUID workspaceId,
			UUID ownerId,
			String query,
			int page,
			int size
	) {
		var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<HabitJpaEntity> result = query.isBlank()
				? jpa.findByWorkspaceIdAndOwnerId(workspaceId, ownerId, pageable)
				: jpa.findByWorkspaceIdAndOwnerIdAndTitleContainingIgnoreCase(
						workspaceId, ownerId, query, pageable);
		return toPage(result);
	}

	@Override
	public Optional<Habit> findByIdAndOwnerId(UUID id, UUID ownerId) {
		return jpa.findByIdAndOwnerId(id, ownerId).map(this::toDomain);
	}

	@Override
	public void delete(Habit habit) {
		jpa.deleteById(habit.id());
	}

	private HabitPage toPage(Page<HabitJpaEntity> result) {
		return new HabitPage(
				result.getContent().stream().map(this::toDomain).toList(),
				result.getNumber(),
				result.getSize(),
				result.getTotalElements(),
				result.getTotalPages()
		);
	}

	private Habit toDomain(HabitJpaEntity entity) {
		return new Habit(
				entity.getId(),
				entity.getOwnerId(),
				entity.getWorkspaceId(),
				entity.getTitle(),
				entity.getCreatedAt(),
				entity.getStreak(),
				entity.getLastCompletedOn()
		);
	}
}
