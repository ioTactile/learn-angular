package com.learn.api.infrastructure.security;

import com.learn.api.application.auth.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class BcryptPasswordHasher implements PasswordHasher {

	private final PasswordEncoder passwordEncoder;
	private final String dummyHash;

	BcryptPasswordHasher(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
		this.dummyHash = passwordEncoder.encode("timing-dummy");
	}

	@Override
	public String hash(String rawPassword) {
		return passwordEncoder.encode(rawPassword);
	}

	@Override
	public boolean matches(String rawPassword, String passwordHash) {
		return passwordEncoder.matches(rawPassword, passwordHash);
	}

	@Override
	public String dummyHash() {
		return dummyHash;
	}
}
