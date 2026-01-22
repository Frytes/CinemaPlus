package com.frytes.cinemaPlus.users;

import com.frytes.cinemaPlus.BaseIntegrationTest;
import com.frytes.cinemaPlus.users.entity.User;
import com.frytes.cinemaPlus.users.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DisplayName("🔐 Интеграционные тесты Авторизации")
class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;

    @Test
    @DisplayName("✅ Регистрация должна проходить успешно и сохранять Email в нижнем регистре")
    void shouldRegisterUser_AndNormalizeEmail() throws Exception {
        String mixedCaseEmail = "SuperUser@Cinema.COM";
        String requestJson = """
            {
                "username": "SuperUser",
                "email": "%s",
                "password": "password123"
            }
        """.formatted(mixedCaseEmail);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        User savedUser = userRepository.findByEmail("superuser@cinema.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("superuser@cinema.com");
        assertThat(savedUser.getRole().name()).isEqualTo("USER");
    }
}
