package com.learn.api.application.auth;

public record LoginUserCommand(String email, String password) {
}
