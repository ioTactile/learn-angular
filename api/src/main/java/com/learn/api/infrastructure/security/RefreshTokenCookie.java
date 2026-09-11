package com.learn.api.infrastructure.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCookie {

	public static final String NAME = "refreshToken";
	public static final String PATH = "/api/auth";

	private final JwtProperties properties;

	public RefreshTokenCookie(JwtProperties properties) {
		this.properties = properties;
	}

	public void write(HttpServletResponse response, String rawRefreshToken) {
		response.addHeader(HttpHeaders.SET_COOKIE, cookie(rawRefreshToken, properties.refreshExpirationDays()).toString());
	}

	public void clear(HttpServletResponse response) {
		response.addHeader(HttpHeaders.SET_COOKIE, cookie("", 0).toString());
	}

	public Optional<String> read(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return Optional.empty();
		}
		return Arrays.stream(cookies)
				.filter(cookie -> NAME.equals(cookie.getName()))
				.map(cookie -> cookie.getValue())
				.filter(value -> value != null && !value.isBlank())
				.findFirst();
	}

	private ResponseCookie cookie(String value, long maxAgeDays) {
		return ResponseCookie.from(NAME, value)
				.httpOnly(true)
				.secure(properties.cookieSecure())
				.sameSite("Lax")
				.path(PATH)
				.maxAge(Duration.ofDays(maxAgeDays))
				.build();
	}
}
