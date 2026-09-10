package com.learn.api.habit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class HabitIT {

	@Autowired
	MockMvc mockMvc;

	@Test
	@DisplayName("POST/GET /api/habits sans token → 401")
	void habits_withoutToken_returnUnauthorized() throws Exception {
		mockMvc.perform(get("/api/habits"))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/habits")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "title": "Drink water" }
								"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("crée un habit puis le liste pour le user connecté")
	void createAndList_ownHabits() throws Exception {
		String token = registerAndGetToken("habits-owner@example.com", "Secret123!");

		mockMvc.perform(post("/api/habits")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "title": "Drink water" }
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isString())
				.andExpect(jsonPath("$.title").value("Drink water"))
				.andExpect(jsonPath("$.streak").value(0));

		mockMvc.perform(get("/api/habits")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].title").value("Drink water"));
	}

	@Test
	@DisplayName("un user ne voit pas les habits d'un autre")
	void list_isIsolatedPerUser() throws Exception {
		String aliceToken = registerAndGetToken("alice-habits@example.com", "Secret123!");
		String bobToken = registerAndGetToken("bob-habits@example.com", "Secret123!");

		mockMvc.perform(post("/api/habits")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + aliceToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "title": "Alice habit" }
								"""))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/habits")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + bobToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(0));
	}

	@Test
	@DisplayName("POST /api/habits refuse un titre vide")
	void create_blankTitle_returnsBadRequest() throws Exception {
		String token = registerAndGetToken("blank-title@example.com", "Secret123!");

		mockMvc.perform(post("/api/habits")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "title": "   " }
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("DELETE /api/habits/{id} supprime mon habit ; celui d'un autre → 404")
	void delete_ownHabit_andForeignReturnsNotFound() throws Exception {
		String aliceToken = registerAndGetToken("alice-delete@example.com", "Secret123!");
		String bobToken = registerAndGetToken("bob-delete@example.com", "Secret123!");

		String habitId = createHabit(aliceToken, "To delete");

		mockMvc.perform(delete("/api/habits/" + habitId)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + bobToken))
				.andExpect(status().isNotFound());

		mockMvc.perform(delete("/api/habits/" + habitId)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + aliceToken))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/habits")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + aliceToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(0));
	}

	@Test
	@DisplayName("POST /api/habits/{id}/complete incrémente le streak")
	void complete_incrementsStreak() throws Exception {
		String token = registerAndGetToken("complete@example.com", "Secret123!");
		String habitId = createHabit(token, "Meditate");

		mockMvc.perform(post("/api/habits/" + habitId + "/complete")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.streak").value(1))
				.andExpect(jsonPath("$.lastCompletedOn").isNotEmpty());

		mockMvc.perform(post("/api/habits/" + habitId + "/complete")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.streak").value(1));
	}

	@Test
	@DisplayName("complete le habit d'un autre → 404")
	void complete_foreignHabit_returnsNotFound() throws Exception {
		String aliceToken = registerAndGetToken("alice-complete@example.com", "Secret123!");
		String bobToken = registerAndGetToken("bob-complete@example.com", "Secret123!");
		String habitId = createHabit(aliceToken, "Secret habit");

		mockMvc.perform(post("/api/habits/" + habitId + "/complete")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + bobToken))
				.andExpect(status().isNotFound());
	}

	private String createHabit(String token, String title) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/habits")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "title": "%s" }
								""".formatted(title)))
				.andExpect(status().isCreated())
				.andReturn();

		return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
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
