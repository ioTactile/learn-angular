package com.learn.api.api.me;

import com.learn.api.application.auth.UserRepository;
import com.learn.api.domain.user.User;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Endpoint protégé de smoke-test auth.
 * Le subject JWT = user id (posé par JwtAuthenticationFilter).
 */
@RestController
@RequestMapping("/api/me")
public class MeController {

	private final UserRepository users;

	public MeController(UserRepository users) {
		this.users = users;
	}

	@GetMapping
	public MeResponse me(Authentication authentication) {
		UUID userId = UUID.fromString(authentication.getName());
		User user = users.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

		return new MeResponse(user.id().toString(), user.email(), user.role().name());
	}
}
