package com.learn.api.application.auth;

/**
 * Résultat du register : token prêt à être renvoyé au client.
 */
public record AuthTokenResult(String accessToken, String tokenType) {

	public static AuthTokenResult bearer(String accessToken) {
		return new AuthTokenResult(accessToken, "Bearer");
	}
}
