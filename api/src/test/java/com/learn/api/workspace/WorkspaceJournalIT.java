package com.learn.api.workspace;

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
class WorkspaceJournalIT {

	@Autowired
	MockMvc mockMvc;

	@Test
	@DisplayName("register → Perso ; create workspace ; journal filtrable from/to")
	void workspacesAndJournalFlow() throws Exception {
		String token = registerAndGetToken("ws-journal@example.com", "Secret123!");

		mockMvc.perform(get("/api/workspaces")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].name").value("Perso"));

		MvcResult createdWs = mockMvc.perform(post("/api/workspaces")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "name": "Sport" }
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("Sport"))
				.andReturn();
		String sportId = JsonPath.read(createdWs.getResponse().getContentAsString(), "$.id");

		MvcResult habitResult = mockMvc.perform(post("/api/habits")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "workspaceId": "%s", "title": "Run" }
								""".formatted(sportId)))
				.andExpect(status().isCreated())
				.andReturn();
		String habitId = JsonPath.read(habitResult.getResponse().getContentAsString(), "$.id");

		mockMvc.perform(post("/api/habits/" + habitId + "/complete")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "note": "5k easy" }
								"""))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/habits/" + habitId)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Run"))
				.andExpect(jsonPath("$.workspaceId").value(sportId));

		mockMvc.perform(get("/api/habits/" + habitId + "/completions")
						.param("from", "2020-01-01")
						.param("to", "2099-12-31")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].note").value("5k easy"));
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
