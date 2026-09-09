package com.learn.api.api.auth;

import com.learn.api.application.auth.AuthTokenResult;
import com.learn.api.application.auth.LoginUserCommand;
import com.learn.api.application.auth.LoginUserUseCase;
import com.learn.api.application.auth.RegisterUserCommand;
import com.learn.api.application.auth.RegisterUserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Couche API = adapters HTTP.
 * Le controller ne contient quasi aucune logique : il délègue au use case.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final RegisterUserUseCase registerUser;
	private final LoginUserUseCase loginUser;

	public AuthController(RegisterUserUseCase registerUser, LoginUserUseCase loginUser) {
		this.registerUser = registerUser;
		this.loginUser = loginUser;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
		AuthTokenResult result = registerUser.execute(
				new RegisterUserCommand(request.email(), request.password())
		);
		return new AuthResponse(result.accessToken(), result.tokenType());
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		AuthTokenResult result = loginUser.execute(
				new LoginUserCommand(request.email(), request.password())
		);
		return new AuthResponse(result.accessToken(), result.tokenType());
	}
}
