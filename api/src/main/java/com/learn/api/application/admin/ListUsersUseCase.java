package com.learn.api.application.admin;

import com.learn.api.application.auth.UserRepository;
import com.learn.api.domain.user.User;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ListUsersUseCase {

	private final UserRepository users;

	public ListUsersUseCase(UserRepository users) {
		this.users = users;
	}

	public List<User> execute() {
		return users.findAll();
	}
}
