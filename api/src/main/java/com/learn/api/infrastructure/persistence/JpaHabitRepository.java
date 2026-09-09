package com.learn.api.infrastructure.persistence;

import com.learn.api.application.habit.HabitRepository;
import com.learn.api.domain.habit.Habit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
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
	public Optional<Habit> findByIdAndOwnerId(UUID id, UUID ownerId) {
		return jpa.findByIdAndOwnerId(id, ownerId).map(this::toDomain);
	}

	@Override
	public void delete(Habit habit) {
		jpa.deleteById(habit.id());
	}

	private Habit toDomain(HabitJpaEntity entity) {
		return new Habit(
				entity.getId(),
				entity.getOwnerId(),
				entity.getTitle(),
				entity.getCreatedAt(),
				entity.getStreak(),
				entity.getLastCompletedOn()
		);
	}
}
