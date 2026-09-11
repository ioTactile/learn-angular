package com.learn.api.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class AuthRateLimitFilterTest {

	private final Clock clock = Clock.fixed(Instant.parse("2026-09-11T12:00:00Z"), ZoneOffset.UTC);
	private final AuthRateLimitFilter filter = new AuthRateLimitFilter(
			new AuthRateLimitProperties(3, 60),
			clock
	);

	@Test
	@DisplayName("autorise jusqu'à maxAttempts puis 429")
	void login_isRateLimitedAfterMaxAttempts() throws Exception {
		FilterChain chain = mock(FilterChain.class);

		for (int i = 0; i < 3; i++) {
			MockHttpServletResponse ok = new MockHttpServletResponse();
			filter.doFilter(loginRequest(), ok, chain);
			assertThat(ok.getStatus()).isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
		}

		MockHttpServletResponse blocked = new MockHttpServletResponse();
		filter.doFilter(loginRequest(), blocked, chain);

		assertThat(blocked.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
		assertThat(blocked.getHeader("Retry-After")).isEqualTo("60");
		assertThat(blocked.getContentAsString()).contains("Too many authentication attempts");
		verify(chain, times(3)).doFilter(any(), any());
	}

	@Test
	@DisplayName("GET n'est pas limité")
	void get_isNotRateLimited() throws Exception {
		FilterChain chain = mock(FilterChain.class);
		MockHttpServletRequest get = new MockHttpServletRequest("GET", "/api/auth/login");
		get.setRemoteAddr("10.0.0.1");
		MockHttpServletResponse response = new MockHttpServletResponse();

		filter.doFilter(get, response, chain);

		verify(chain).doFilter(get, response);
		assertThat(response.getStatus()).isNotEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
	}

	private static MockHttpServletRequest loginRequest() {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
		request.setRemoteAddr("10.0.0.1");
		return request;
	}
}
