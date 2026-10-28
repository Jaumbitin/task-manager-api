package com.portfolio.taskmanagerapi;

import com.portfolio.taskmanagerapi.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApiFlowTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired TaskRepository tasks;

    String register(String email) throws Exception {
        String body = "{\"email\":\"" + email + "\",\"password\":\"Teste123!\"}";
        mvc.perform(post("/api/users").contentType("application/json").content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.password").doesNotExist());
        return mvc.perform(post("/api/auth/login").contentType("application/json").content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    @Test
    void completeLifecycle() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        String token = register(email);
        UUID userId = users.findByEmail(email).orElseThrow().getId();
        assertThat(users.findById(userId).orElseThrow().getPassword()).doesNotContain("Teste123!");
        mvc.perform(post("/api/tasks").header("Authorization", "Bearer " + token)
                .contentType("application/json").content("{\"title\":\"Estudar Java\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDING"));
        UUID id = tasks.findByUserId(userId).getFirst().getId();
        mvc.perform(get("/api/tasks").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].title").value("Estudar Java"));
        mvc.perform(put("/api/tasks/" + id).header("Authorization", "Bearer " + token)
                .contentType("application/json").content("{\"title\":\"Estudar Spring\",\"description\":\"API\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Estudar Spring"));
        mvc.perform(patch("/api/tasks/" + id + "/status").header("Authorization", "Bearer " + token)
                .contentType("application/json").content("{\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"));
        mvc.perform(delete("/api/tasks/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        assertThat(tasks.findById(id)).isEmpty();
    }

    @Test
    void usersCannotAccessEachOthersTasks() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        String first = register(email);
        UUID ownerId = users.findByEmail(email).orElseThrow().getId();
        mvc.perform(post("/api/tasks").header("Authorization", "Bearer " + first)
                .contentType("application/json").content("{\"title\":\"Privada\"}")).andExpect(status().isCreated());
        UUID taskId = tasks.findByUserId(ownerId).getFirst().getId();
        String second = register(UUID.randomUUID() + "@example.com");
        mvc.perform(get("/api/tasks").header("Authorization", "Bearer " + second))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(get("/api/tasks/user/" + ownerId).header("Authorization", "Bearer " + second))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/tasks/" + ownerId).header("Authorization", "Bearer " + second)
                .contentType("application/json").content("{\"title\":\"Invasao\"}")).andExpect(status().isForbidden());
        mvc.perform(put("/api/tasks/" + taskId).header("Authorization", "Bearer " + second)
                .contentType("application/json").content("{\"title\":\"Invasao\"}")).andExpect(status().isNotFound());
        mvc.perform(patch("/api/tasks/" + taskId + "/status").header("Authorization", "Bearer " + second)
                .contentType("application/json").content("{\"status\":\"COMPLETED\"}")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/tasks/" + taskId).header("Authorization", "Bearer " + second))
                .andExpect(status().isNotFound());
        assertThat(tasks.findById(taskId)).isPresent();
    }

    @Test
    void authenticationValidationAndCors() throws Exception {
        mvc.perform(get("/api/tasks")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/tasks").header("Authorization", "Bearer invalid")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType("application/json")
                .content("{\"email\":\"missing@example.com\",\"password\":\"wrong\"}")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/users").contentType("application/json")
                .content("{\"email\":\"invalid\",\"password\":\"\"}")).andExpect(status().isBadRequest());
        String email = UUID.randomUUID() + "@example.com";
        String token = register(email);
        mvc.perform(post("/api/users").contentType("application/json")
                .content("{\"email\":\"" + email + "\",\"password\":\"Teste123!\"}")).andExpect(status().isConflict());
        mvc.perform(post("/api/auth/login").contentType("application/json")
                .content("{\"email\":\"" + email + "\",\"password\":\"wrong\"}")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/tasks").header("Authorization", "Bearer " + token)
                .contentType("application/json").content("{\"title\":\"\"}")).andExpect(status().isBadRequest());
        mvc.perform(patch("/api/tasks/" + UUID.randomUUID() + "/status").header("Authorization", "Bearer " + token)
                .contentType("application/json").content("{}")).andExpect(status().isBadRequest());
        mvc.perform(options("/api/tasks").header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void rejectsOversizedInputAndMalformedAuthorization() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        String token = register(email);
        mvc.perform(get("/api/tasks").header("Authorization", token)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/tasks").header("Authorization", "bearer " + token)).andExpect(status().isOk());
        mvc.perform(put("/api/tasks/not-a-uuid").header("Authorization", "Bearer " + token)
                .contentType("application/json").content("{\"title\":\"ok\"}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/tasks").header("Authorization", "Bearer " + token)
                .contentType("application/json").content("{\"title\":\"" + "x".repeat(256) + "\"}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/tasks").header("Authorization", "Bearer " + token)
                .contentType("application/json").content("{\"title\":\"ok\",\"description\":\"" + "x".repeat(2001) + "\"}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/users").contentType("application/json")
                .content("{\"email\":\"long@example.com\",\"password\":\"" + "é".repeat(37) + "\"}")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/users").contentType("application/json")
                .content("{\"email\":\"short@example.com\",\"password\":\"123\"}")).andExpect(status().isBadRequest());
    }
}
