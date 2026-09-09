package com.learn.api.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.learn.api.domain.user.InvalidCredentialsException;
import com.learn.api.domain.user.User;
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
	TokenProvider tokenProvider;

	LoginUserUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new LoginUserUseCase(users, passwordHasher, tokenProvider);
	}

	@Test
	@DisplayName("login valide renvoie un token Bearer")
	void execute_withValidCredentials_returnsToken() {
		UUID id = UUID.randomUUID();
		User user = new User(id, "alice@example.com", "hashed", Instant.now());

		when(users.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
		when(passwordHasher.matches("Secret123!", "hashed")).thenReturn(true);
		when(tokenProvider.issueAccessToken(id, "alice@example.com")).thenReturn("jwt-token");

		AuthTokenResult result = useCase.execute(new LoginUserCommand("Alice@Example.com", "Secret123!"));

		assertThat(result.accessToken()).isEqualTo("jwt-token");
		assertThat(result.tokenType()).isEqualTo("Bearer");
	}

	@Test
	@DisplayName("email inconnu → InvalidCredentials (pas d'énumération d'emails)")
	void execute_whenUserMissing_throwsInvalidCredentials() {
		when(users.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.execute(new LoginUserCommand("ghost@example.com", "Secret123!")))
				.isInstanceOf(InvalidCredentialsException.class);

		verifyNoInteractions(passwordHasher, tokenProvider);
	}

	@Test
	@DisplayName("mauvais mot de passe → InvalidCredentials")
	void execute_whenPasswordWrong_throwsInvalidCredentials() {
		User user = new User(UUID.randomUUID(), "alice@example.com", "hashed", Instant.now());
		when(users.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
		when(passwordHasher.matches("wrong-password", "hashed")).thenReturn(false);

		assertThatThrownBy(() -> useCase.execute(new LoginUserCommand("alice@example.com", "wrong-password")))
				.isInstanceOf(InvalidCredentialsException.class);

		verifyNoInteractions(tokenProvider);
	}
}
