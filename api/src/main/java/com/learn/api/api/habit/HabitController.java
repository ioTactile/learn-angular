package com.learn.api.api.habit;

import com.learn.api.application.habit.CompleteHabitUseCase;
import com.learn.api.application.habit.CreateHabitCommand;
import com.learn.api.application.habit.CreateHabitUseCase;
import com.learn.api.application.habit.DeleteHabitUseCase;
import com.learn.api.application.habit.HabitPage;
import com.learn.api.application.habit.ListHabitsQuery;
import com.learn.api.application.habit.ListHabitsUseCase;
import com.learn.api.domain.habit.Habit;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/habits")
public class HabitController {

	private final CreateHabitUseCase createHabit;
	private final ListHabitsUseCase listHabits;
	private final DeleteHabitUseCase deleteHabit;
	private final CompleteHabitUseCase completeHabit;

	public HabitController(
			CreateHabitUseCase createHabit,
			ListHabitsUseCase listHabits,
			DeleteHabitUseCase deleteHabit,
			CompleteHabitUseCase completeHabit
	) {
		this.createHabit = createHabit;
		this.listHabits = listHabits;
		this.deleteHabit = deleteHabit;
		this.completeHabit = completeHabit;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public HabitResponse create(@Valid @RequestBody CreateHabitRequest request, Authentication authentication) {
		UUID ownerId = currentUserId(authentication);
		Habit habit = createHabit.execute(new CreateHabitCommand(ownerId, request.title()));
		return toResponse(habit);
	}

	@GetMapping
	public HabitPageResponse list(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String q,
			Authentication authentication
	) {
		HabitPage result = listHabits.execute(
				new ListHabitsQuery(currentUserId(authentication), q, page, size)
		);
		return new HabitPageResponse(
				result.content().stream().map(this::toResponse).toList(),
				result.page(),
				result.size(),
				result.totalElements(),
				result.totalPages()
		);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id, Authentication authentication) {
		deleteHabit.execute(id, currentUserId(authentication));
	}

	@PostMapping("/{id}/complete")
	public HabitResponse complete(@PathVariable UUID id, Authentication authentication) {
		Habit habit = completeHabit.execute(id, currentUserId(authentication));
		return toResponse(habit);
	}

	private static UUID currentUserId(Authentication authentication) {
		return UUID.fromString(authentication.getName());
	}

	private HabitResponse toResponse(Habit habit) {
		return new HabitResponse(
				habit.id().toString(),
				habit.title(),
				habit.createdAt(),
				habit.streak(),
				habit.lastCompletedOn()
		);
	}
}
