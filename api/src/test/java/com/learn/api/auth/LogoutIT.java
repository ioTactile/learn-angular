package com.learn.api.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.learn.api.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
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
	@DisplayName("POST /api/auth/logout révoque le refresh : un refresh suivant → 401")
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

		String refreshToken = JsonPath.read(register.getResponse().getContentAsString(), "$.refreshToken");

		mockMvc.perform(post("/api/auth/logout")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "refreshToken": "%s" }
								""".formatted(refreshToken)))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "refreshToken": "%s" }
								""".formatted(refreshToken)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("logout d'un token inconnu → 204 (idempotent)")
	void logout_unknownToken_isIdempotent() throws Exception {
		mockMvc.perform(post("/api/auth/logout")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "refreshToken": "not-a-real-token" }
								"""))
				.andExpect(status().isNoContent());
	}

	@Test
	@DisplayName("logout sans refreshToken → 400")
	void logout_blankToken_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/auth/logout")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "refreshToken": "" }
								"""))
				.andExpect(status().isBadRequest());
	}
}
