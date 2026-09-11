package com.learn.api.infrastructure.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
		@NotBlank @Size(min = 32, max = 256) String secret,
		@Positive long expirationMinutes,
		@Positive long refreshExpirationDays,
		@NotBlank String issuer,
		@NotBlank String audience,
		boolean cookieSecure
) {
}
