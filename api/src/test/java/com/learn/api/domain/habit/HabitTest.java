package com.learn.api.domain.habit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HabitTest {

	@Test
	@DisplayName("première completion → streak 1")
	void complete_firstTime_setsStreakToOne() {
		Habit habit = Habit.create(UUID.randomUUID(), UUID.randomUUID(), "Run");
		LocalDate today = LocalDate.of(2026, 9, 9);

		Habit completed = habit.complete(today);

		assertThat(completed.streak()).isEqualTo(1);
		assertThat(completed.lastCompletedOn()).isEqualTo(today);
	}

	@Test
	@DisplayName("jour suivant → streak incrémenté")
	void complete_consecutiveDay_incrementsStreak() {
		Habit habit = new Habit(
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				"Run",
				Instant.parse("2026-09-01T00:00:00Z"),
				2,
				LocalDate.of(2026, 9, 8)
		);

		Habit completed = habit.complete(LocalDate.of(2026, 9, 9));

		assertThat(completed.streak()).isEqualTo(3);
	}

	@Test
	@DisplayName("même jour → idempotent")
	void complete_sameDay_keepsStreak() {
		LocalDate today = LocalDate.of(2026, 9, 9);
		Habit habit = new Habit(
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				"Run",
				Instant.parse("2026-09-01T00:00:00Z"),
				4,
				today
		);

		Habit completed = habit.complete(today);

		assertThat(completed.streak()).isEqualTo(4);
		assertThat(completed.lastCompletedOn()).isEqualTo(today);
	}

	@Test
	@DisplayName("jour sauté → streak repart à 1")
	void complete_afterGap_resetsStreak() {
		Habit habit = new Habit(
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				"Run",
				Instant.parse("2026-09-01T00:00:00Z"),
				5,
				LocalDate.of(2026, 9, 1)
		);

		Habit completed = habit.complete(LocalDate.of(2026, 9, 9));

		assertThat(completed.streak()).isEqualTo(1);
	}
}
