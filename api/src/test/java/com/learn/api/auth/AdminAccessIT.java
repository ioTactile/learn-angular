package com.learn.api.auth;

import static org.hamcrest.Matchers.hasItem;
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
class AdminAccessIT {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	JdbcTemplate jdbc;

	@Test
	@DisplayName("USER → 403 sur /api/admin/users ; ADMIN → 200")
	void adminUsers_requiresAdminRole() throws Exception {
		String userToken = registerAndGetToken("user-role@example.com", "Secret123!");

		mockMvc.perform(get("/api/admin/users")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isForbidden());

		jdbc.update("UPDATE users SET role = 'ADMIN' WHERE email = ?", "user-role@example.com");

		MvcResult login = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "user-role@example.com",
								  "password": "Secret123!"
								}
								"""))
				.andExpect(status().isOk())
				.andReturn();

		String adminToken = JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");

		mockMvc.perform(get("/api/admin/users")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[*].email").value(hasItem("user-role@example.com")))
				.andExpect(jsonPath("$[?(@.email=='user-role@example.com')].role").value(hasItem("ADMIN")));
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
