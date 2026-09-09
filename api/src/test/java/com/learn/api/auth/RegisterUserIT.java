package com.learn.api.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.learn.api.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Test d'intégration = contrat HTTP réel + Postgres réel (Testcontainers).
 *
 * Équivalent Node : un test Fastify avec inject()/supertest + Postgres Docker.
 * On commence RED : ce test doit échouer tant que /api/auth/register n'existe pas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class RegisterUserIT {

	@Autowired
	MockMvc mockMvc;

	@Test
	@DisplayName("POST /api/auth/register crée un compte et renvoie un JWT")
	void register_returnsCreatedAndAccessToken() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "alice@example.com",
								  "password": "Secret123!"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.accessToken").isString())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andReturn();

		String body = result.getResponse().getContentAsString();
		assertThat(body).contains("accessToken");
	}

	@Test
	@DisplayName("POST /api/auth/register refuse un email déjà utilisé")
	void register_duplicateEmail_returnsConflict() throws Exception {
		String payload = """
				{
				  "email": "bob@example.com",
				  "password": "Secret123!"
				}
				""";

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payload))
				.andExpect(status().isConflict());
	}

	@Test
	@DisplayName("POST /api/auth/register valide email et mot de passe")
	void register_invalidPayload_returnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "not-an-email",
								  "password": "short"
								}
								"""))
				.andExpect(status().isBadRequest());
	}
}
