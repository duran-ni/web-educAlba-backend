package dev.duran.web_educAlba_backend.academy.teacher;

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
class TeacherControllerTest {

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
    @DisplayName("A new teacher can be created")
    void testCreate_ReturnsCreated() throws Exception {
        TeacherRequest request = new TeacherRequest("Laura Gómez", "Matemáticas");

        mockMvc.perform(post("/api/admin/teachers")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Laura Gómez"))
                .andExpect(jsonPath("$.specialty").value("Matemáticas"));
    }

    @Test
    @DisplayName("All teachers can be listed")
    void testGetAll_ReturnsOk() throws Exception {
        TeacherRequest request = new TeacherRequest("Marcos Ruiz", "Educación Física");

        mockMvc.perform(post("/api/admin/teachers")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/admin/teachers")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("A single teacher can be retrieved by id")
    void testGetById_ReturnsOk() throws Exception {
        TeacherRequest createRequest = new TeacherRequest("Ana Torres", "Inglés");

        String createBody = mockMvc.perform(post("/api/admin/teachers")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        TeacherResponse created = mapper.readValue(createBody, TeacherResponse.class);

        mockMvc.perform(get("/api/admin/teachers/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Torres"));
    }

    @Test
    @DisplayName("Retrieving a teacher that does not exist returns Not Found")
    void testGetById_NonExistentTeacher_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/admin/teachers/999999")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("An existing teacher can be updated")
    void testUpdate_ReturnsOk() throws Exception {
        TeacherRequest createRequest = new TeacherRequest("Pedro Sánchez", "Historia");

        String createBody = mockMvc.perform(post("/api/admin/teachers")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        TeacherResponse created = mapper.readValue(createBody, TeacherResponse.class);
        TeacherRequest updateRequest = new TeacherRequest("Pedro Sánchez", "Geografía");

        mockMvc.perform(put("/api/admin/teachers/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialty").value("Geografía"));
    }

    @Test
    @DisplayName("Updating a teacher that does not exist returns Not Found")
    void testUpdate_NonExistentTeacher_ReturnsNotFound() throws Exception {
        TeacherRequest updateRequest = new TeacherRequest("No importa", "No importa");

        mockMvc.perform(put("/api/admin/teachers/999999")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("A deleted teacher can no longer be found")
    void testDelete_ThenGetReturnsNotFound() throws Exception {
        TeacherRequest createRequest = new TeacherRequest("Sofía Díaz", "Música");

        String createBody = mockMvc.perform(post("/api/admin/teachers")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        TeacherResponse created = mapper.readValue(createBody, TeacherResponse.class);

        mockMvc.perform(delete("/api/admin/teachers/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/teachers/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deleting a teacher that does not exist returns Not Found")
    void testDelete_NonExistentTeacher_ReturnsNotFound() throws Exception {
        mockMvc.perform(delete("/api/admin/teachers/999999")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Creating a teacher with a missing required field is rejected")
    void testCreate_MissingRequiredField_ReturnsBadRequest() throws Exception {
        String invalidJson = "{\"specialty\": \"Falta el nombre\"}";

        mockMvc.perform(post("/api/admin/teachers")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Accessing the admin teachers endpoint without authentication is rejected")
    void testGetAll_WithoutAuthentication_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/teachers"))
                .andExpect(status().isUnauthorized());
    }
}
