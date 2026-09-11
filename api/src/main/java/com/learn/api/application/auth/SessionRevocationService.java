package com.learn.api.application.auth;

import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionRevocationService {

	private final RefreshTokenStore refreshTokens;
	private final UserRepository users;

	public SessionRevocationService(RefreshTokenStore refreshTokens, UserRepository users) {
		this.refreshTokens = refreshTokens;
		this.users = users;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void revokeAll(UUID userId) {
		users.incrementTokenVersion(userId);
		refreshTokens.deleteAllByUserId(userId);
	}
}
