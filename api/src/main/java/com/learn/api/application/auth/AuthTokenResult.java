package com.learn.api.application.auth;

/**
 * Résultat auth : access JWT court + refresh opaque longue durée.
 */
public record AuthTokenResult(String accessToken, String refreshToken, String tokenType) {

	public static AuthTokenResult bearer(String accessToken, String refreshToken) {
		return new AuthTokenResult(accessToken, refreshToken, "Bearer");
	}
}
