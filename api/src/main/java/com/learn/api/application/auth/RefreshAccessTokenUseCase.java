package com.learn.api.application.auth;

import com.learn.api.domain.user.InvalidCredentialsException;
import com.learn.api.domain.user.User;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefreshAccessTokenUseCase {

	private final RefreshTokenStore refreshTokens;
	private final UserRepository users;
	private final AuthSessionService sessions;
	private final Clock clock;

	public RefreshAccessTokenUseCase(
			RefreshTokenStore refreshTokens,
			UserRepository users,
			AuthSessionService sessions,
			Clock clock
	) {
		this.refreshTokens = refreshTokens;
		this.users = users;
		this.sessions = sessions;
		this.clock = clock;
	}

	@Transactional
	public AuthTokenResult execute(String rawRefreshToken) {
		Instant now = clock.instant();
		String hash = AuthSessionService.sha256(rawRefreshToken);

		var stored = refreshTokens.findValidByHash(hash, now)
				.orElseThrow(InvalidCredentialsException::new);

		User user = users.findById(stored.userId())
				.orElseThrow(InvalidCredentialsException::new);

		// Rotation : invalide l'ancien refresh, en émet un nouveau
		refreshTokens.deleteByHash(hash);
		return sessions.openSession(user);
	}
}
