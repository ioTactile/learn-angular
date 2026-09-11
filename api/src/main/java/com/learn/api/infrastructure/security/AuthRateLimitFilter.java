package com.learn.api.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Plafond d'essais sur login / register / refresh, par IP et par route.
 */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

	private static final Set<String> PROTECTED_PATHS = Set.of(
			"/api/auth/login",
			"/api/auth/register",
			"/api/auth/refresh"
	);

	private final AuthRateLimitProperties properties;
	private final Clock clock;
	private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

	public AuthRateLimitFilter(AuthRateLimitProperties properties, Clock clock) {
		this.properties = properties;
		this.clock = clock;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !"POST".equalsIgnoreCase(request.getMethod())
				|| !PROTECTED_PATHS.contains(request.getRequestURI());
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String key = request.getRemoteAddr() + ":" + request.getRequestURI();
		if (!tryConsume(key)) {
			writeTooManyRequests(response);
			return;
		}
		filterChain.doFilter(request, response);
	}

	private boolean tryConsume(String key) {
		long now = clock.millis();
		long windowMs = properties.windowSeconds() * 1000L;
		Window next = windows.compute(key, (ignored, current) -> {
			if (current == null || now - current.startMillis() >= windowMs) {
				return new Window(now, 1);
			}
			return new Window(current.startMillis(), current.count() + 1);
		});
		return next.count() <= properties.maxAttempts();
	}

	private void writeTooManyRequests(HttpServletResponse response) throws IOException {
		response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
		response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
		response.setCharacterEncoding(StandardCharsets.UTF_8.name());
		response.setHeader("Retry-After", String.valueOf(properties.windowSeconds()));
		String body = """
				{"title":"Too Many Requests","status":429,"detail":"Too many authentication attempts","timestamp":"%s"}
				""".formatted(Instant.now(clock));
		response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
	}

	private record Window(long startMillis, int count) {
	}
}
