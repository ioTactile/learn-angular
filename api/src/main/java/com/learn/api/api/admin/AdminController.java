package com.learn.api.api.admin;

import com.learn.api.application.admin.ListUsersUseCase;
import com.learn.api.domain.user.User;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private final ListUsersUseCase listUsers;

	public AdminController(ListUsersUseCase listUsers) {
		this.listUsers = listUsers;
	}

	@GetMapping("/users")
	public List<AdminUserResponse> listUsers() {
		return listUsers.execute().stream().map(this::toResponse).toList();
	}

	private AdminUserResponse toResponse(User user) {
		return new AdminUserResponse(
				user.id().toString(),
				user.email(),
				user.role().name(),
				user.createdAt().toString()
		);
	}
}
