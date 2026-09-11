package com.learn.api.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.learn.api.TestcontainersConfiguration;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class LogoutIT {

	@Autowired
	MockMvc mockMvc;

	@Test
	@DisplayName("POST /api/auth/logout révoque refresh + access")
	void logout_revokesRefreshToken() throws Exception {
		MvcResult register = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "logout-ok@example.com",
								  "password": "Secret123!"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn();

		Cookie refresh = AuthCookies.refreshCookie(register);
		String access = JsonPath.read(register.getResponse().getContentAsString(), "$.accessToken");

		mockMvc.perform(post("/api/auth/logout").cookie(refresh))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/auth/refresh").cookie(refresh))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + access))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("logout sans cookie → 204 (idempotent)")
	void logout_withoutCookie_isIdempotent() throws Exception {
		mockMvc.perform(post("/api/auth/logout"))
				.andExpect(status().isNoContent());
	}
}
