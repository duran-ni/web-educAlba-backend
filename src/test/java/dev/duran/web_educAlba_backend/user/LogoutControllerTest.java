package dev.duran.web_educAlba_backend.user;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import dev.duran.web_educAlba_backend.TestcontainersConfiguration;
import tools.jackson.databind.ObjectMapper;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LogoutControllerTest {

    private static final String EMAIL = "logout@educalba.com";
    private static final String PLAIN_PASSWORD = "ClaveSegura123";

    @Autowired
    WebApplicationContext context;

    @Autowired
    ObjectMapper mapper;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();

        // Registramos un usuario real una sola vez; si ya existe de un test anterior, se ignora el 409
        String encodedPassword = Base64.getEncoder().encodeToString(PLAIN_PASSWORD.getBytes());
        RegisterRequest request = new RegisterRequest(EMAIL, encodedPassword);

        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(request)));
    }

    @Test
    @DisplayName("An authenticated user can log out")
    void testLogout_ReturnsNoContent() throws Exception {
        mockMvc.perform(post("/api/auth/logout").with(httpBasic(EMAIL, PLAIN_PASSWORD)))
            .andExpect(status().isNoContent());
    }
}
