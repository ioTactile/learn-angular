package com.learn.api.infrastructure.persistence;

import com.learn.api.application.habit.HabitCompletionRepository;
import com.learn.api.domain.habit.HabitCompletion;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class JpaHabitCompletionRepository implements HabitCompletionRepository {

	private final HabitCompletionJpaRepository jpa;

	JpaHabitCompletionRepository(HabitCompletionJpaRepository jpa) {
		this.jpa = jpa;
	}

	@Override
	public HabitCompletion save(HabitCompletion completion) {
		HabitCompletionJpaEntity saved = jpa.save(new HabitCompletionJpaEntity(
				completion.id(),
				completion.habitId(),
				completion.completedOn(),
				completion.note(),
				completion.createdAt()
		));
		return toDomain(saved);
	}

	@Override
	public Optional<HabitCompletion> findByHabitIdAndCompletedOn(UUID habitId, LocalDate completedOn) {
		return jpa.findByHabitIdAndCompletedOn(habitId, completedOn).map(this::toDomain);
	}

	@Override
	public List<HabitCompletion> findByHabitIdAndCompletedOnBetween(
			UUID habitId,
			LocalDate from,
			LocalDate to
	) {
		return jpa.findByHabitIdAndCompletedOnBetweenOrderByCompletedOnDesc(habitId, from, to).stream()
				.map(this::toDomain)
				.toList();
	}

	private HabitCompletion toDomain(HabitCompletionJpaEntity entity) {
		return new HabitCompletion(
				entity.getId(),
				entity.getHabitId(),
				entity.getCompletedOn(),
				entity.getNote(),
				entity.getCreatedAt()
		);
	}
}
