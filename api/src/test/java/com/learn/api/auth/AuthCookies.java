package com.learn.api.auth;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.Cookie;
import org.springframework.test.web.servlet.MvcResult;

final class AuthCookies {

	private AuthCookies() {
	}

	static Cookie refreshCookie(MvcResult result) {
		Cookie cookie = result.getResponse().getCookie("refreshToken");
		assertThat(cookie).isNotNull();
		assertThat(cookie.getValue()).isNotBlank();
		assertThat(cookie.isHttpOnly()).isTrue();
		return cookie;
	}
}
