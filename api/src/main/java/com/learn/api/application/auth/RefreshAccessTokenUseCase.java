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
	private final SessionRevocationService revocations;
	private final Clock clock;

	public RefreshAccessTokenUseCase(
			RefreshTokenStore refreshTokens,
			UserRepository users,
			AuthSessionService sessions,
			SessionRevocationService revocations,
			Clock clock
	) {
		this.refreshTokens = refreshTokens;
		this.users = users;
		this.sessions = sessions;
		this.revocations = revocations;
		this.clock = clock;
	}

	@Transactional
	public AuthTokenResult execute(String rawRefreshToken) {
		Instant now = clock.instant();
		String hash = AuthSessionService.sha256(rawRefreshToken);

		var stored = refreshTokens.findByHash(hash)
				.orElseThrow(InvalidCredentialsException::new);

		if (stored.revoked()) {
			revocations.revokeAll(stored.userId());
			throw new InvalidCredentialsException();
		}

		if (stored.expired(now)) {
			throw new InvalidCredentialsException();
		}

		User user = users.findById(stored.userId())
				.orElseThrow(InvalidCredentialsException::new);

		refreshTokens.revokeByHash(hash, now);
		return sessions.openSession(user);
	}
}
