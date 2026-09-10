package com.learn.api.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.learn.api.domain.user.EmailAlreadyRegisteredException;
import com.learn.api.domain.user.User;
import com.learn.api.domain.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

	@Mock
	UserRepository users;

	@Mock
	PasswordHasher passwordHasher;

	@Mock
	AuthSessionService sessions;

	RegisterUserUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new RegisterUserUseCase(users, passwordHasher, sessions);
	}

	@Test
	@DisplayName("enregistre un utilisateur et ouvre une session")
	void execute_persistsHashedPasswordAndReturnsToken() {
		when(users.existsByEmail("alice@example.com")).thenReturn(false);
		when(passwordHasher.hash("Secret123!")).thenReturn("hashed");
		when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(sessions.openSession(any(User.class)))
				.thenReturn(AuthTokenResult.bearer("jwt-token", "refresh-token"));

		AuthTokenResult result = useCase.execute(new RegisterUserCommand("Alice@Example.com", "Secret123!"));

		assertThat(result.accessToken()).isEqualTo("jwt-token");
		assertThat(result.refreshToken()).isEqualTo("refresh-token");
		assertThat(result.tokenType()).isEqualTo("Bearer");

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(users).save(captor.capture());
		assertThat(captor.getValue().email()).isEqualTo("alice@example.com");
		assertThat(captor.getValue().passwordHash()).isEqualTo("hashed");
		assertThat(captor.getValue().role()).isEqualTo(UserRole.USER);
	}

	@Test
	@DisplayName("refuse un email déjà enregistré")
	void execute_whenEmailExists_throws() {
		when(users.existsByEmail("bob@example.com")).thenReturn(true);

		assertThatThrownBy(() -> useCase.execute(new RegisterUserCommand("bob@example.com", "Secret123!")))
				.isInstanceOf(EmailAlreadyRegisteredException.class);

		verify(users, never()).save(any());
	}
}
