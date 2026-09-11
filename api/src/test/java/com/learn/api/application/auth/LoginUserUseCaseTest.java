package com.learn.api.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.learn.api.domain.user.InvalidCredentialsException;
import com.learn.api.domain.user.User;
import com.learn.api.domain.user.UserRole;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoginUserUseCaseTest {

	@Mock
	UserRepository users;

	@Mock
	PasswordHasher passwordHasher;

	@Mock
	AuthSessionService sessions;

	LoginUserUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new LoginUserUseCase(users, passwordHasher, sessions);
	}

	@Test
	@DisplayName("login valide ouvre une session Bearer")
	void execute_withValidCredentials_returnsToken() {
		UUID id = UUID.randomUUID();
		User user = new User(id, "alice@example.com", "hashed", UserRole.USER, Instant.now(), 0);

		when(users.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
		when(passwordHasher.matches("Secret123!", "hashed")).thenReturn(true);
		when(sessions.openSession(user)).thenReturn(AuthTokenResult.bearer("jwt-token", "refresh-token"));

		AuthTokenResult result = useCase.execute(new LoginUserCommand("Alice@Example.com", "Secret123!"));

		assertThat(result.accessToken()).isEqualTo("jwt-token");
		assertThat(result.refreshToken()).isEqualTo("refresh-token");
		assertThat(result.tokenType()).isEqualTo("Bearer");
	}

	@Test
	@DisplayName("email inconnu → InvalidCredentials (hash factice pour le timing)")
	void execute_whenUserMissing_throwsInvalidCredentials() {
		when(users.findByEmail("ghost@example.com")).thenReturn(Optional.empty());
		when(passwordHasher.dummyHash()).thenReturn("dummy-hash");
		when(passwordHasher.matches("Secret123!", "dummy-hash")).thenReturn(false);

		assertThatThrownBy(() -> useCase.execute(new LoginUserCommand("ghost@example.com", "Secret123!")))
				.isInstanceOf(InvalidCredentialsException.class);

		verifyNoInteractions(sessions);
	}

	@Test
	@DisplayName("mauvais mot de passe → InvalidCredentials")
	void execute_whenPasswordWrong_throwsInvalidCredentials() {
		User user = new User(UUID.randomUUID(), "alice@example.com", "hashed", UserRole.USER, Instant.now(), 0);
		when(users.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
		when(passwordHasher.matches("wrong-password", "hashed")).thenReturn(false);

		assertThatThrownBy(() -> useCase.execute(new LoginUserCommand("alice@example.com", "wrong-password")))
				.isInstanceOf(InvalidCredentialsException.class);

		verifyNoInteractions(sessions);
	}
}
