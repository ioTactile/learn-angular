package com.learn.api.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "habits")
class HabitJpaEntity {

	@Id
	private UUID id;

	@Column(name = "owner_id", nullable = false)
	private UUID ownerId;

	@Column(name = "workspace_id", nullable = false)
	private UUID workspaceId;

	@Column(nullable = false, length = 120)
	private String title;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private int streak;

	@Column(name = "last_completed_on")
	private LocalDate lastCompletedOn;

	protected HabitJpaEntity() {
	}

	HabitJpaEntity(
			UUID id,
			UUID ownerId,
			UUID workspaceId,
			String title,
			Instant createdAt,
			int streak,
			LocalDate lastCompletedOn
	) {
		this.id = id;
		this.ownerId = ownerId;
		this.workspaceId = workspaceId;
		this.title = title;
		this.createdAt = createdAt;
		this.streak = streak;
		this.lastCompletedOn = lastCompletedOn;
	}

	UUID getId() {
		return id;
	}

	UUID getOwnerId() {
		return ownerId;
	}

	UUID getWorkspaceId() {
		return workspaceId;
	}

	String getTitle() {
		return title;
	}

	Instant getCreatedAt() {
		return createdAt;
	}

	int getStreak() {
		return streak;
	}

	LocalDate getLastCompletedOn() {
		return lastCompletedOn;
	}
}
