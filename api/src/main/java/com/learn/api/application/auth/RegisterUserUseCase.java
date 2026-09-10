package com.learn.api.application.auth;

import com.learn.api.domain.user.EmailAlreadyRegisteredException;
import com.learn.api.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUserUseCase {

	private final UserRepository users;
	private final PasswordHasher passwordHasher;
	private final AuthSessionService sessions;

	public RegisterUserUseCase(
			UserRepository users,
			PasswordHasher passwordHasher,
			AuthSessionService sessions
	) {
		this.users = users;
		this.passwordHasher = passwordHasher;
		this.sessions = sessions;
	}

	@Transactional
	public AuthTokenResult execute(RegisterUserCommand command) {
		String email = command.email().trim().toLowerCase();

		if (users.existsByEmail(email)) {
			throw new EmailAlreadyRegisteredException(email);
		}

		User user = User.create(email, passwordHasher.hash(command.password()));
		User saved = users.save(user);
		return sessions.openSession(saved);
	}
}
