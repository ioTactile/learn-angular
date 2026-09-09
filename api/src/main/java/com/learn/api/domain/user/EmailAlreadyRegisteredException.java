package com.learn.api.domain.user;

/**
 * Exception métier : l'email est déjà pris.
 * Équivalent d'une erreur 409 côté HTTP (mappée dans la couche api).
 */
public class EmailAlreadyRegisteredException extends RuntimeException {

	public EmailAlreadyRegisteredException(String email) {
		super("Email already registered: " + email);
	}
}
