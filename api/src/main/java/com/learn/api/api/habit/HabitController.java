package com.learn.api.api.habit;

import com.learn.api.application.habit.CompleteHabitUseCase;
import com.learn.api.application.habit.CreateHabitCommand;
import com.learn.api.application.habit.CreateHabitUseCase;
import com.learn.api.application.habit.DeleteHabitUseCase;
import com.learn.api.application.habit.GetHabitUseCase;
import com.learn.api.application.habit.HabitPage;
import com.learn.api.application.habit.ListHabitCompletionsUseCase;
import com.learn.api.application.habit.ListHabitsQuery;
import com.learn.api.application.habit.ListHabitsUseCase;
import com.learn.api.domain.habit.Habit;
import com.learn.api.domain.habit.HabitCompletion;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
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
	private final GetHabitUseCase getHabit;
	private final DeleteHabitUseCase deleteHabit;
	private final CompleteHabitUseCase completeHabit;
	private final ListHabitCompletionsUseCase listCompletions;

	public HabitController(
			CreateHabitUseCase createHabit,
			ListHabitsUseCase listHabits,
			GetHabitUseCase getHabit,
			DeleteHabitUseCase deleteHabit,
			CompleteHabitUseCase completeHabit,
			ListHabitCompletionsUseCase listCompletions
	) {
		this.createHabit = createHabit;
		this.listHabits = listHabits;
		this.getHabit = getHabit;
		this.deleteHabit = deleteHabit;
		this.completeHabit = completeHabit;
		this.listCompletions = listCompletions;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public HabitResponse create(@Valid @RequestBody CreateHabitRequest request, Authentication authentication) {
		UUID ownerId = currentUserId(authentication);
		Habit habit = createHabit.execute(
				new CreateHabitCommand(ownerId, request.workspaceId(), request.title())
		);
		return toResponse(habit);
	}

	@GetMapping
	public HabitPageResponse list(
			@RequestParam UUID workspaceId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String q,
			Authentication authentication
	) {
		HabitPage result = listHabits.execute(
				new ListHabitsQuery(currentUserId(authentication), workspaceId, q, page, size)
		);
		return new HabitPageResponse(
				result.content().stream().map(this::toResponse).toList(),
				result.page(),
				result.size(),
				result.totalElements(),
				result.totalPages()
		);
	}

	@GetMapping("/{id}")
	public HabitResponse get(@PathVariable UUID id, Authentication authentication) {
		return toResponse(getHabit.execute(id, currentUserId(authentication)));
	}

	@GetMapping("/{id}/completions")
	public List<HabitCompletionResponse> completions(
			@PathVariable UUID id,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			Authentication authentication
	) {
		return listCompletions.execute(id, currentUserId(authentication), from, to).stream()
				.map(this::toCompletionResponse)
				.toList();
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID id, Authentication authentication) {
		deleteHabit.execute(id, currentUserId(authentication));
	}

	@PostMapping("/{id}/complete")
	public HabitResponse complete(
			@PathVariable UUID id,
			@RequestBody(required = false) @Valid CompleteHabitRequest request,
			Authentication authentication
	) {
		String note = request == null ? null : request.note();
		Habit habit = completeHabit.execute(id, currentUserId(authentication), note);
		return toResponse(habit);
	}

	private static UUID currentUserId(Authentication authentication) {
		return UUID.fromString(authentication.getName());
	}

	private HabitResponse toResponse(Habit habit) {
		return new HabitResponse(
				habit.id().toString(),
				habit.workspaceId().toString(),
				habit.title(),
				habit.createdAt(),
				habit.streak(),
				habit.lastCompletedOn()
		);
	}

	private HabitCompletionResponse toCompletionResponse(HabitCompletion completion) {
		return new HabitCompletionResponse(
				completion.id().toString(),
				completion.habitId().toString(),
				completion.completedOn(),
				completion.note(),
				completion.createdAt()
		);
	}
}
