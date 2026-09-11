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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class LoginUserIT {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

	@Test
	@DisplayName("POST /api/auth/login renvoie un JWT pour des identifiants valides")
	void login_withValidCredentials_returnsToken() throws Exception {
		register("carol@example.com", "Secret123!");

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "carol@example.com",
								  "password": "Secret123!"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isString())
				.andExpect(jsonPath("$.tokenType").value("Bearer"));
	}

	@Test
	@DisplayName("POST /api/auth/login refuse un mauvais mot de passe avec 401")
	void login_withWrongPassword_returnsUnauthorized() throws Exception {
		register("dave@example.com", "Secret123!");

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "dave@example.com",
								  "password": "WrongPass1"
								}
								"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("GET /api/me sans token → 401 ; avec Bearer → 200")
	void me_requiresBearerToken() throws Exception {
		String token = registerAndGetToken("erin@example.com", "Secret123!");

		mockMvc.perform(get("/api/me"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("erin@example.com"))
				.andExpect(jsonPath("$.role").value("USER"));
	}

	@Test
	@DisplayName("GET /api/me avec JWT d'un user supprimé → 401")
	void me_deletedUser_returnsUnauthorized() throws Exception {
		String token = registerAndGetToken("gone@example.com", "Secret123!");
		jdbc.update("DELETE FROM users WHERE email = ?", "gone@example.com");

		mockMvc.perform(get("/api/me")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isUnauthorized());
	}

	private void register(String email, String password) throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "%s"
								}
								""".formatted(email, password)))
				.andExpect(status().isCreated());
	}

	private String registerAndGetToken(String email, String password) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "%s"
								}
								""".formatted(email, password)))
				.andExpect(status().isCreated())
				.andReturn();

		return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
	}
}
