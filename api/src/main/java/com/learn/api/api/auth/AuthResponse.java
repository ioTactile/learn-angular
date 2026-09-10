package com.learn.api.api.auth;

public record AuthResponse(String accessToken, String refreshToken, String tokenType) {
}
