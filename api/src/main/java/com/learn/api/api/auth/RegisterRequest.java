package com.learn.api.api.auth;

import com.learn.api.domain.user.PasswordRules;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO d'entrée HTTP (= body JSON).
 * Bean Validation ≈ Zod / class-validator côté Node.
 */
public record RegisterRequest(
		@NotBlank @Email String email,
		@NotBlank
		@Size(min = 8, max = 72)
		@Pattern(regexp = PasswordRules.REGEX, message = PasswordRules.MESSAGE)
		String password
) {
}
