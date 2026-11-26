package com.dore.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(properties = { "app.user=prueba", "app.password=clave-de-prueba" })
@AutoConfigureMockMvc
@Testcontainers
class SecurityTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    MockMvc mvc;

    @Test
    void theApiRequiresASession() throws Exception {
        mvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/csrf")).andExpect(status().isNoContent());
    }

    @Test
    void onlyTheRightPasswordLogsIn() throws Exception {
        mvc.perform(formLogin("/api/auth/login").user("prueba").password("clave-de-prueba"))
                .andExpect(status().isNoContent());
        mvc.perform(formLogin("/api/auth/login").user("prueba").password("otra"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void changesNeedTheCsrfToken() throws Exception {
        mvc.perform(post("/api/products").with(user("prueba"))
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/products").with(user("prueba")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}
