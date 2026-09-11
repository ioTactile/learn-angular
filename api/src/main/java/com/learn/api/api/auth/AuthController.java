package com.learn.api.api.auth;

import com.learn.api.application.auth.AuthTokenResult;
import com.learn.api.application.auth.LoginUserCommand;
import com.learn.api.application.auth.LoginUserUseCase;
import com.learn.api.application.auth.LogoutUserUseCase;
import com.learn.api.application.auth.RefreshAccessTokenUseCase;
import com.learn.api.application.auth.RegisterUserCommand;
import com.learn.api.application.auth.RegisterUserUseCase;
import com.learn.api.domain.user.InvalidCredentialsException;
import com.learn.api.infrastructure.security.RefreshTokenCookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final RegisterUserUseCase registerUser;
	private final LoginUserUseCase loginUser;
	private final RefreshAccessTokenUseCase refreshAccessToken;
	private final LogoutUserUseCase logoutUser;
	private final RefreshTokenCookie refreshCookie;

	public AuthController(
			RegisterUserUseCase registerUser,
			LoginUserUseCase loginUser,
			RefreshAccessTokenUseCase refreshAccessToken,
			LogoutUserUseCase logoutUser,
			RefreshTokenCookie refreshCookie
	) {
		this.registerUser = registerUser;
		this.loginUser = loginUser;
		this.refreshAccessToken = refreshAccessToken;
		this.logoutUser = logoutUser;
		this.refreshCookie = refreshCookie;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthResponse register(
			@Valid @RequestBody RegisterRequest request,
			HttpServletResponse response
	) {
		return toResponse(registerUser.execute(
				new RegisterUserCommand(request.email(), request.password())
		), response);
	}

	@PostMapping("/login")
	public AuthResponse login(
			@Valid @RequestBody LoginRequest request,
			HttpServletResponse response
	) {
		return toResponse(loginUser.execute(
				new LoginUserCommand(request.email(), request.password())
		), response);
	}

	@PostMapping("/refresh")
	public AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) {
		String raw = refreshCookie.read(request).orElseThrow(InvalidCredentialsException::new);
		return toResponse(refreshAccessToken.execute(raw), response);
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(HttpServletRequest request, HttpServletResponse response) {
		refreshCookie.read(request).ifPresent(logoutUser::execute);
		refreshCookie.clear(response);
	}

	private AuthResponse toResponse(AuthTokenResult result, HttpServletResponse response) {
		refreshCookie.write(response, result.refreshToken());
		return new AuthResponse(result.accessToken(), result.tokenType());
	}
}
