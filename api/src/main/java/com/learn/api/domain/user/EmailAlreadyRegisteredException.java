package com.learn.api.domain.user;

/**
 * Exception métier : l'email est déjà pris.
 * Message volontairement générique (pas d'énumération de l'adresse).
 */
public class EmailAlreadyRegisteredException extends RuntimeException {

	public EmailAlreadyRegisteredException() {
		super("Email already registered");
	}
}
