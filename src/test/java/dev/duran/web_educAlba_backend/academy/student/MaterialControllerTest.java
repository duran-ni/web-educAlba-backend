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
class MaterialControllerTest {

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

    private Long createStudent() throws Exception {
        StudentRequest request = new StudentRequest("Carlos", "Diaz", "Refuerzo", EducationalStage.ESO);

        String body = mockMvc.perform(post("/api/admin/students")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return mapper.readValue(body, StudentResponse.class).id();
    }

    @Test
    @DisplayName("A new material can be created")
    void testCreate_ReturnsCreated() throws Exception {
        Long studentId = createStudent();
        MaterialRequest request = new MaterialRequest("Worksheet 1", "Mathematics", 1024L, "/materials/worksheet1.pdf", studentId);

        mockMvc.perform(post("/api/admin/materials")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Worksheet 1"))
            .andExpect(jsonPath("$.studentFullName").value("Carlos Diaz"));
    }

    @Test
    @DisplayName("All materials can be listed")
    void testGetAll_ReturnsOk() throws Exception {
        Long studentId = createStudent();
        MaterialRequest request = new MaterialRequest("Reading guide", "Language", 512L, "/materials/reading-guide.pdf", studentId);

        mockMvc.perform(post("/api/admin/materials")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/admin/materials")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("An existing material can be updated")
    void testUpdate_ReturnsOk() throws Exception {
        Long studentId = createStudent();
        MaterialRequest createRequest = new MaterialRequest("Draft notes", "Science", 256L, "/materials/draft-notes.pdf", studentId);

        String createBody = mockMvc.perform(post("/api/admin/materials")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        MaterialResponse created = mapper.readValue(createBody, MaterialResponse.class);
        MaterialRequest updateRequest = new MaterialRequest("Final notes", "Science", 300L, "/materials/final-notes.pdf", studentId);

        mockMvc.perform(put("/api/admin/materials/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Final notes"));
    }

    @Test
    @DisplayName("A deleted material can no longer be found")
    void testDelete_ThenGetReturnsNotFound() throws Exception {
        Long studentId = createStudent();
        MaterialRequest createRequest = new MaterialRequest("Temporary file", "History", 128L, "/materials/temp.pdf", studentId);

        String createBody = mockMvc.perform(post("/api/admin/materials")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        MaterialResponse created = mapper.readValue(createBody, MaterialResponse.class);

        mockMvc.perform(delete("/api/admin/materials/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/materials/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Creating a material with a missing required field is rejected")
    void testCreate_MissingRequiredField_ReturnsBadRequest() throws Exception {
        String invalidJson = "{\"subject\": \"Mathematics\", \"fileSize\": 100}";

        mockMvc.perform(post("/api/admin/materials")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }
}
