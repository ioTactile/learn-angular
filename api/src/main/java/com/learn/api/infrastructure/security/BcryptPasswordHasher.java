package com.learn.api.infrastructure.security;

import com.learn.api.application.auth.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class BcryptPasswordHasher implements PasswordHasher {

	private final PasswordEncoder passwordEncoder;

	BcryptPasswordHasher(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public String hash(String rawPassword) {
		return passwordEncoder.encode(rawPassword);
	}

	@Override
	public boolean matches(String rawPassword, String passwordHash) {
		return passwordEncoder.matches(rawPassword, passwordHash);
	}
}
