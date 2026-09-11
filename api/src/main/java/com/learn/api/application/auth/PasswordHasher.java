package com.learn.api.application.auth;

/**
 * Port : hash / verify des mots de passe (implémenté avec BCrypt en infra).
 */
public interface PasswordHasher {

	String hash(String rawPassword);

	boolean matches(String rawPassword, String passwordHash);

	/** Hash fixe pour égaliser le temps de réponse si l'email n'existe pas. */
	String dummyHash();
}
