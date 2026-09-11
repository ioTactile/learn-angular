package com.learn.api.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface HabitCompletionJpaRepository extends JpaRepository<HabitCompletionJpaEntity, UUID> {

	Optional<HabitCompletionJpaEntity> findByHabitIdAndCompletedOn(UUID habitId, LocalDate completedOn);

	List<HabitCompletionJpaEntity> findByHabitIdAndCompletedOnBetweenOrderByCompletedOnDesc(
			UUID habitId,
			LocalDate from,
			LocalDate to
	);
}
