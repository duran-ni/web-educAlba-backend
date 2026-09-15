package dev.duran.web_educAlba_backend.academy;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class StudentControllerTest {

    private static final String ADMIN_EMAIL = "admin@educalba.com";
    private static final String ADMIN_PASSWORD = "AdminEducAlba123";

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
    @DisplayName("A new student can be created")
    void testCreate_ReturnsCreated() throws Exception {
        StudentRequest request = new StudentRequest("Ana", "Garcia", "Refuerzo", EducationalStage.PRIMARIA);

        mockMvc.perform(post("/api/admin/students")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.firstName").value("Ana"))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("All students can be listed")
    void testGetAll_ReturnsOk() throws Exception {
        StudentRequest request = new StudentRequest("Luis", "Perez", "Talleres", EducationalStage.ESO);

        mockMvc.perform(post("/api/admin/students")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/admin/students")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("An existing student can be updated")
    void testUpdate_ReturnsOk() throws Exception {
        StudentRequest createRequest = new StudentRequest("Marta", "Ruiz", "Refuerzo", EducationalStage.INFANTIL);

        String createBody = mockMvc.perform(post("/api/admin/students")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        StudentResponse created = mapper.readValue(createBody, StudentResponse.class);
        StudentRequest updateRequest = new StudentRequest("Marta", "Ruiz Gomez", "Talleres", EducationalStage.PRIMARIA);

        mockMvc.perform(put("/api/admin/students/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lastName").value("Ruiz Gomez"));
    }

    @Test
    @DisplayName("A deleted student can no longer be found")
    void testDelete_ThenGetReturnsNotFound() throws Exception {
        StudentRequest createRequest = new StudentRequest("Pedro", "Lopez", "Refuerzo", EducationalStage.ESO);

        String createBody = mockMvc.perform(post("/api/admin/students")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        StudentResponse created = mapper.readValue(createBody, StudentResponse.class);

        mockMvc.perform(delete("/api/admin/students/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/students/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Creating a student with a missing required field is rejected")
    void testCreate_MissingRequiredField_ReturnsBadRequest() throws Exception {
        String invalidJson = "{\"lastName\": \"Test\", \"educationalStage\": \"PRIMARIA\"}";

        mockMvc.perform(post("/api/admin/students")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }
}
