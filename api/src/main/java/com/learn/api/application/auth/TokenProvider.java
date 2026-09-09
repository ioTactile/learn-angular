package com.learn.api.application.auth;

import java.util.UUID;

/**
 * Port : émission de JWT. La crypto reste en infrastructure.
 */
public interface TokenProvider {

	String issueAccessToken(UUID userId, String email);
}
