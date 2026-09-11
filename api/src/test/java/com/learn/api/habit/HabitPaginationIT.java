package com.learn.api.habit;

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
class HabitPaginationIT {

	@Autowired
	MockMvc mockMvc;

	@Test
	@DisplayName("GET /api/habits?workspaceId pagine et filtre par titre")
	void list_supportsPaginationAndFilter() throws Exception {
		String token = registerAndGetToken("page-filter@example.com", "Secret123!");
		String workspaceId = defaultWorkspaceId(token);

		createHabit(token, workspaceId, "Drink water");
		createHabit(token, workspaceId, "Drink coffee");
		createHabit(token, workspaceId, "Run outside");

		mockMvc.perform(get("/api/habits")
						.param("workspaceId", workspaceId)
						.param("page", "0")
						.param("size", "2")
						.param("q", "drink")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(2))
				.andExpect(jsonPath("$.totalElements").value(2))
				.andExpect(jsonPath("$.totalPages").value(1))
				.andExpect(jsonPath("$.page").value(0))
				.andExpect(jsonPath("$.size").value(2));

		mockMvc.perform(get("/api/habits")
						.param("workspaceId", workspaceId)
						.param("page", "0")
						.param("size", "10")
						.param("q", "run")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].title").value("Run outside"))
				.andExpect(jsonPath("$.totalElements").value(1));
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

	private String defaultWorkspaceId(String token) throws Exception {
		MvcResult result = mockMvc.perform(get("/api/workspaces")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andReturn();
		return JsonPath.read(result.getResponse().getContentAsString(), "$[0].id");
	}

	private void createHabit(String token, String workspaceId, String title) throws Exception {
		mockMvc.perform(post("/api/habits")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "workspaceId": "%s", "title": "%s" }
								""".formatted(workspaceId, title)))
				.andExpect(status().isCreated());
	}
}
