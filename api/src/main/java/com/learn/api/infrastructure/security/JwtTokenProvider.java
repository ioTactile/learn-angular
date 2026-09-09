package com.learn.api.infrastructure.security;

import com.learn.api.application.auth.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider implements TokenProvider {

	private final JwtProperties properties;
	private final SecretKey key;

	public JwtTokenProvider(JwtProperties properties) {
		this.properties = properties;
		this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public String issueAccessToken(UUID userId, String email) {
		Instant now = Instant.now();
		Instant expires = now.plusSeconds(properties.expirationMinutes() * 60);

		return Jwts.builder()
				.subject(userId.toString())
				.claim("email", email)
				.issuedAt(Date.from(now))
				.expiration(Date.from(expires))
				.signWith(key)
				.compact();
	}

	public Claims parse(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
