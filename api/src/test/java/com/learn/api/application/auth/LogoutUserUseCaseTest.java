package com.learn.api.application.auth;

import static org.mockito.Mockito.verify;

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

	LogoutUserUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new LogoutUserUseCase(refreshTokens);
	}

	@Test
	@DisplayName("logout supprime le refresh hashé, même s'il est inconnu")
	void execute_deletesHashedRefreshToken() {
		useCase.execute("raw-refresh");

		verify(refreshTokens).deleteByHash(AuthSessionService.sha256("raw-refresh"));
	}
}
