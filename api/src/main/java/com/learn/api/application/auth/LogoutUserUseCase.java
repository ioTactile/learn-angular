package com.learn.api.application.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Révoque un refresh token. L'access JWT reste valide jusqu'à expiration (stateless).
 * Idempotent : un token inconnu ne lève pas d'erreur.
 */
@Service
public class LogoutUserUseCase {

	private final RefreshTokenStore refreshTokens;

	public LogoutUserUseCase(RefreshTokenStore refreshTokens) {
		this.refreshTokens = refreshTokens;
	}

	@Transactional
	public void execute(String rawRefreshToken) {
		refreshTokens.deleteByHash(AuthSessionService.sha256(rawRefreshToken));
	}
}
