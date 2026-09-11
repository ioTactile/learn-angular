package com.learn.api.infrastructure.security;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.security.rate-limit.auth")
public record AuthRateLimitProperties(
		@Positive int maxAttempts,
		@Positive long windowSeconds
) {
}
