package com.learn.api.api;

import com.learn.api.domain.habit.HabitNotFoundException;
import com.learn.api.domain.user.EmailAlreadyRegisteredException;
import com.learn.api.domain.user.InvalidCredentialsException;
import com.learn.api.domain.workspace.WorkspaceNotFoundException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduit les exceptions métier / validation en réponses HTTP stables (RFC 7807).
 * Équivalent d'un setErrorHandler Fastify.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(EmailAlreadyRegisteredException.class)
	ProblemDetail handleEmailAlreadyRegistered(EmailAlreadyRegisteredException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
		problem.setTitle("Email already registered");
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
		problem.setTitle("Unauthorized");
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(HabitNotFoundException.class)
	ProblemDetail handleHabitNotFound(HabitNotFoundException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		problem.setTitle("Habit not found");
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(WorkspaceNotFoundException.class)
	ProblemDetail handleWorkspaceNotFound(WorkspaceNotFoundException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
		problem.setTitle("Workspace not found");
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST,
				"Request validation failed"
		);
		problem.setTitle("Validation failed");

		Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
				.collect(java.util.stream.Collectors.toMap(
						fieldError -> fieldError.getField(),
						fieldError -> fieldError.getDefaultMessage() == null ? "invalid" : fieldError.getDefaultMessage(),
						(left, right) -> left
				));
		problem.setProperty("errors", errors);
		problem.setProperty("timestamp", Instant.now());
		return problem;
	}
}
