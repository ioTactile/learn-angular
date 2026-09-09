package com.learn.api.domain.user;

/**
 * Identifiants invalides — message volontairement générique
 * pour ne pas révéler si l'email existe (anti user-enumeration).
 */
public class InvalidCredentialsException extends RuntimeException {

	public InvalidCredentialsException() {
		super("Invalid email or password");
	}
}
