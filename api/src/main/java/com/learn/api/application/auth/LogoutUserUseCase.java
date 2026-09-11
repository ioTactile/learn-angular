package com.learn.api.application.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Révoque la session : tous les refresh + bump de token_version (access JWT morts).
 * Idempotent : un token inconnu ne lève pas d'erreur.
 */
@Service
public class LogoutUserUseCase {

	private final RefreshTokenStore refreshTokens;
	private final SessionRevocationService revocations;

	public LogoutUserUseCase(RefreshTokenStore refreshTokens, SessionRevocationService revocations) {
		this.refreshTokens = refreshTokens;
		this.revocations = revocations;
	}

	@Transactional
	public void execute(String rawRefreshToken) {
		refreshTokens.findByHash(AuthSessionService.sha256(rawRefreshToken))
				.ifPresent(stored -> revocations.revokeAll(stored.userId()));
	}
}
