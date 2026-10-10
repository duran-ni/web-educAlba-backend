package dev.duran.web_educAlba_backend.academy;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
class InterestSubmissionControllerTest {

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
    @DisplayName("A visitor can submit the quick interest form without authentication")
    void testCreate_ReturnsCreated() throws Exception {
        InterestSubmissionRequest request = new InterestSubmissionRequest("Juanito", "Matemáticas 2º ESO");

        mockMvc.perform(post("/api/public/interest-submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.studentName").value("Juanito"))
            .andExpect(jsonPath("$.courseToReinforce").value("Matemáticas 2º ESO"));
    }

    @Test
    @DisplayName("Submitting the form without the student name is rejected")
    void testCreate_MissingStudentName_ReturnsBadRequest() throws Exception {
        String invalidJson = "{\"courseToReinforce\": \"Lengua 3º Primaria\"}";

        mockMvc.perform(post("/api/public/interest-submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }
}
