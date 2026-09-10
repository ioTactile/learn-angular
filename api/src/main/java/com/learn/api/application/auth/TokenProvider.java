package com.learn.api.application.auth;

import java.util.UUID;

/**
 * Port : émission de JWT access + refresh opaque.
 */
public interface TokenProvider {

	String issueAccessToken(UUID userId, String email, String role);

	/** Token opaque aléatoire (jamais un JWT) — stocké hashé en base. */
	String issueRefreshToken();
}
