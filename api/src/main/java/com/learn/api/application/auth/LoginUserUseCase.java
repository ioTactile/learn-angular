package com.learn.api.application.auth;

import com.learn.api.domain.user.InvalidCredentialsException;
import com.learn.api.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginUserUseCase {

	private final UserRepository users;
	private final PasswordHasher passwordHasher;
	private final AuthSessionService sessions;

	public LoginUserUseCase(
			UserRepository users,
			PasswordHasher passwordHasher,
			AuthSessionService sessions
	) {
		this.users = users;
		this.passwordHasher = passwordHasher;
		this.sessions = sessions;
	}

	@Transactional
	public AuthTokenResult execute(LoginUserCommand command) {
		String email = command.email().trim().toLowerCase();

		User user = users.findByEmail(email)
				.orElseThrow(InvalidCredentialsException::new);

		if (!passwordHasher.matches(command.password(), user.passwordHash())) {
			throw new InvalidCredentialsException();
		}

		return sessions.openSession(user);
	}
}
