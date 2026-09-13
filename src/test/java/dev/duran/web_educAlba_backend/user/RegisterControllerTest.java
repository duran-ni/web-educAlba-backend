package dev.duran.web_educAlba_backend.user;

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
class RegisterControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    ObjectMapper mapper;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
    }

    @Test
    @DisplayName("A new user can register successfully")
    void testRegister_ReturnsCreated() throws Exception {
        String encodedPassword = Base64.getEncoder().encodeToString("ClaveSegura123".getBytes());
        RegisterRequest request = new RegisterRequest("new" + System.currentTimeMillis() + "@educalba.com", encodedPassword);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("The same email cannot be registered twice")
    void testRegister_DuplicateEmail_ReturnsConflict() throws Exception {
        String encodedPassword = Base64.getEncoder().encodeToString("ClaveSegura123".getBytes());
        RegisterRequest request = new RegisterRequest("duplicado@educalba.com", encodedPassword);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("A password that is too short is rejected")
    void testRegister_PasswordTooShort_ReturnsBadRequest() throws Exception {
        String encodedShortPassword = Base64.getEncoder().encodeToString("abc123".getBytes());
        RegisterRequest request = new RegisterRequest("corta@educalba.com", encodedShortPassword);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
