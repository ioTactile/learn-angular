package com.learn.api.application.auth;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
class LogoutUserUseCaseTest {

	@Mock
	RefreshTokenStore refreshTokens;

	@Mock
	SessionRevocationService revocations;

	LogoutUserUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new LogoutUserUseCase(refreshTokens, revocations);
	}

	@Test
	@DisplayName("logout connu : révoque toutes les sessions")
	void execute_revokesAllSessionsForUser() {
		UUID userId = UUID.randomUUID();
		when(refreshTokens.findByHash(AuthSessionService.sha256("raw-refresh")))
				.thenReturn(Optional.of(new RefreshTokenStore.StoredRefreshToken(
						UUID.randomUUID(),
						userId,
						"hash",
						Instant.now().plusSeconds(3600),
						null
				)));

		useCase.execute("raw-refresh");

		verify(revocations).revokeAll(userId);
	}
}
