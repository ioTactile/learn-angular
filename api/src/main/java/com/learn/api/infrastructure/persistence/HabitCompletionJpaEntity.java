package com.learn.api.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "habit_completions")
class HabitCompletionJpaEntity {

	@Id
	private UUID id;

	@Column(name = "habit_id", nullable = false)
	private UUID habitId;

	@Column(name = "completed_on", nullable = false)
	private LocalDate completedOn;

	@Column(length = 500)
	private String note;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected HabitCompletionJpaEntity() {
	}

	HabitCompletionJpaEntity(UUID id, UUID habitId, LocalDate completedOn, String note, Instant createdAt) {
		this.id = id;
		this.habitId = habitId;
		this.completedOn = completedOn;
		this.note = note;
		this.createdAt = createdAt;
	}

	UUID getId() {
		return id;
	}

	UUID getHabitId() {
		return habitId;
	}

	LocalDate getCompletedOn() {
		return completedOn;
	}

	String getNote() {
		return note;
	}

	Instant getCreatedAt() {
		return createdAt;
	}
}
