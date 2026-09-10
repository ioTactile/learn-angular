package com.learn.api.api.auth;

import com.learn.api.application.auth.AuthTokenResult;
import com.learn.api.application.auth.LoginUserCommand;
import com.learn.api.application.auth.LoginUserUseCase;
import com.learn.api.application.auth.LogoutUserUseCase;
import com.learn.api.application.auth.RefreshAccessTokenUseCase;
import com.learn.api.application.auth.RegisterUserCommand;
import com.learn.api.application.auth.RegisterUserUseCase;
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

	public AuthController(
			RegisterUserUseCase registerUser,
			LoginUserUseCase loginUser,
			RefreshAccessTokenUseCase refreshAccessToken,
			LogoutUserUseCase logoutUser
	) {
		this.registerUser = registerUser;
		this.loginUser = loginUser;
		this.refreshAccessToken = refreshAccessToken;
		this.logoutUser = logoutUser;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
		return toResponse(registerUser.execute(
				new RegisterUserCommand(request.email(), request.password())
		));
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return toResponse(loginUser.execute(
				new LoginUserCommand(request.email(), request.password())
		));
	}

	@PostMapping("/refresh")
	public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
		return toResponse(refreshAccessToken.execute(request.refreshToken()));
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(@Valid @RequestBody RefreshRequest request) {
		logoutUser.execute(request.refreshToken());
	}

	private static AuthResponse toResponse(AuthTokenResult result) {
		return new AuthResponse(result.accessToken(), result.refreshToken(), result.tokenType());
	}
}
