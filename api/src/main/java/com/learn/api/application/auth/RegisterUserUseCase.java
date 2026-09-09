package com.learn.api.application.auth;

import com.learn.api.domain.user.EmailAlreadyRegisteredException;
import com.learn.api.domain.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case Register = logique métier orchestrée.
 * Équivalent d'un service applicatif Nest : pas de HttpRequest ici.
 */
@Service
public class RegisterUserUseCase {

	private final UserRepository users;
	private final PasswordHasher passwordHasher;
	private final TokenProvider tokenProvider;

	public RegisterUserUseCase(
			UserRepository users,
			PasswordHasher passwordHasher,
			TokenProvider tokenProvider
	) {
		this.users = users;
		this.passwordHasher = passwordHasher;
		this.tokenProvider = tokenProvider;
	}

	@Transactional
	public AuthTokenResult execute(RegisterUserCommand command) {
		String email = command.email().trim().toLowerCase();

		if (users.existsByEmail(email)) {
			throw new EmailAlreadyRegisteredException(email);
		}

		User user = User.create(email, passwordHasher.hash(command.password()));
		User saved = users.save(user);

		String token = tokenProvider.issueAccessToken(saved.id(), saved.email());
		return AuthTokenResult.bearer(token);
	}
}
