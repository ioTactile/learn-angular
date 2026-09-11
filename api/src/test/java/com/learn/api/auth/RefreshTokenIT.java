package com.learn.api.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
class RefreshTokenIT {

	@Autowired
	MockMvc mockMvc;

	@Test
	@DisplayName("register pose un cookie refresh HttpOnly, pas de refresh dans le JSON")
	void register_setsHttpOnlyRefreshCookie() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "refresh-reg@example.com",
								  "password": "Secret123!"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.accessToken").isString())
				.andExpect(jsonPath("$.refreshToken").doesNotExist())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(cookie().exists("refreshToken"))
				.andExpect(cookie().httpOnly("refreshToken", true));
	}

	@Test
	@DisplayName("POST /api/auth/refresh émet un nouvel access token via cookie")
	void refresh_returnsNewAccessToken() throws Exception {
		MvcResult register = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "refresh-ok@example.com",
								  "password": "Secret123!"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn();

		Cookie refresh = AuthCookies.refreshCookie(register);

		MvcResult refreshed = mockMvc.perform(post("/api/auth/refresh")
						.cookie(refresh))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isString())
				.andExpect(jsonPath("$.refreshToken").doesNotExist())
				.andReturn();

		String newAccess = JsonPath.read(refreshed.getResponse().getContentAsString(), "$.accessToken");

		mockMvc.perform(get("/api/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccess))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("refresh-ok@example.com"));
	}

	@Test
	@DisplayName("réutilisation d'un refresh révoqué → 401 et sessions tuées")
	void refresh_reusedToken_returnsUnauthorized() throws Exception {
		MvcResult register = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "refresh-reuse@example.com",
								  "password": "Secret123!"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn();

		Cookie firstRefresh = AuthCookies.refreshCookie(register);
		String access = JsonPath.read(register.getResponse().getContentAsString(), "$.accessToken");

		mockMvc.perform(post("/api/auth/refresh").cookie(firstRefresh))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/auth/refresh").cookie(firstRefresh))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + access))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("refresh sans cookie → 401")
	void refresh_missingCookie_returnsUnauthorized() throws Exception {
		mockMvc.perform(post("/api/auth/refresh"))
				.andExpect(status().isUnauthorized());
	}
}
