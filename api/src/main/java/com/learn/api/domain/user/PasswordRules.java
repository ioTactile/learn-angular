package com.learn.api.domain.user;

public final class PasswordRules {

	public static final String REGEX = "^(?=.*[A-Za-z])(?=.*\\d).{8,72}$";
	public static final String MESSAGE = "must contain a letter and a digit (8-72 characters)";

	private PasswordRules() {
	}
}
