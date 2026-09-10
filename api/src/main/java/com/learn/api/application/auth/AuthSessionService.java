package com.learn.api.application.auth;

import com.learn.api.domain.user.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Crée une session auth (access + refresh) et persiste le refresh hashé.
 */
@Service
public class AuthSessionService {

	private final TokenProvider tokenProvider;
	private final RefreshTokenStore refreshTokens;
	private final JwtSettings jwtSettings;
	private final Clock clock;

	public AuthSessionService(
			TokenProvider tokenProvider,
			RefreshTokenStore refreshTokens,
			JwtSettings jwtSettings,
			Clock clock
	) {
		this.tokenProvider = tokenProvider;
		this.refreshTokens = refreshTokens;
		this.jwtSettings = jwtSettings;
		this.clock = clock;
	}

	@Transactional
	public AuthTokenResult openSession(User user) {
		String access = tokenProvider.issueAccessToken(user.id(), user.email(), user.role().name());
		String refresh = tokenProvider.issueRefreshToken();
		Instant expiresAt = clock.instant().plusSeconds(jwtSettings.refreshExpirationDays() * 24L * 3600L);
		refreshTokens.save(user.id(), sha256(refresh), expiresAt);
		return AuthTokenResult.bearer(access, refresh);
	}

	public static String sha256(String raw) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 unavailable", e);
		}
	}
}
