package com.learn.api.application.auth;

import com.learn.api.application.workspace.WorkspaceRepository;
import com.learn.api.domain.user.EmailAlreadyRegisteredException;
import com.learn.api.domain.user.User;
import com.learn.api.domain.workspace.Workspace;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterUserUseCase {

	private final UserRepository users;
	private final PasswordHasher passwordHasher;
	private final AuthSessionService sessions;
	private final WorkspaceRepository workspaces;

	public RegisterUserUseCase(
			UserRepository users,
			PasswordHasher passwordHasher,
			AuthSessionService sessions,
			WorkspaceRepository workspaces
	) {
		this.users = users;
		this.passwordHasher = passwordHasher;
		this.sessions = sessions;
		this.workspaces = workspaces;
	}

	@Transactional
	public AuthTokenResult execute(RegisterUserCommand command) {
		String email = command.email().trim().toLowerCase();

		if (users.existsByEmail(email)) {
			throw new EmailAlreadyRegisteredException(email);
		}

		User user = User.create(email, passwordHasher.hash(command.password()));
		User saved = users.save(user);
		workspaces.save(Workspace.defaultPersonal(saved.id()));
		return sessions.openSession(saved);
	}
}
