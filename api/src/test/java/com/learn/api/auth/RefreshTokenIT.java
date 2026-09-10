package com.learn.api.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.learn.api.TestcontainersConfiguration;
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
	@DisplayName("register renvoie accessToken + refreshToken")
	void register_returnsRefreshToken() throws Exception {
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
				.andExpect(jsonPath("$.refreshToken").isString())
				.andExpect(jsonPath("$.tokenType").value("Bearer"));
	}

	@Test
	@DisplayName("POST /api/auth/refresh émet un nouvel access token")
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

		String body = register.getResponse().getContentAsString();
		String refreshToken = JsonPath.read(body, "$.refreshToken");

		MvcResult refreshed = mockMvc.perform(post("/api/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "refreshToken": "%s" }
								""".formatted(refreshToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isString())
				.andExpect(jsonPath("$.refreshToken").isString())
				.andReturn();

		String newAccess = JsonPath.read(refreshed.getResponse().getContentAsString(), "$.accessToken");

		mockMvc.perform(get("/api/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccess))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("refresh-ok@example.com"));
	}

	@Test
	@DisplayName("refresh token invalide → 401")
	void refresh_invalidToken_returnsUnauthorized() throws Exception {
		mockMvc.perform(post("/api/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "refreshToken": "not-a-real-token" }
								"""))
				.andExpect(status().isUnauthorized());
	}
}
