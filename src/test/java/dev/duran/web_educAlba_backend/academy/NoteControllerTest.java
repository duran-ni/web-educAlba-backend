package dev.duran.web_educAlba_backend.academy;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

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
class NoteControllerTest {

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
        StudentRequest request = new StudentRequest("Sofia", "Navarro", "Talleres", EducationalStage.INFANTIL);

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
    @DisplayName("A new note can be created")
    void testCreate_ReturnsCreated() throws Exception {
        Long studentId = createStudent();
        NoteRequest request = new NoteRequest("Shows great progress in group activities", LocalDate.now(), true, studentId);

        mockMvc.perform(post("/api/admin/notes")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.studentFullName").value("Sofia Navarro"))
            .andExpect(jsonPath("$.authorEmail").value(ADMIN_EMAIL));
    }

    @Test
    @DisplayName("All notes can be listed")
    void testGetAll_ReturnsOk() throws Exception {
        Long studentId = createStudent();
        NoteRequest request = new NoteRequest("Needs extra support with reading", LocalDate.now(), false, studentId);

        mockMvc.perform(post("/api/admin/notes")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/admin/notes")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("An existing note can be updated")
    void testUpdate_ReturnsOk() throws Exception {
        Long studentId = createStudent();
        NoteRequest createRequest = new NoteRequest("First draft of the note", LocalDate.now(), false, studentId);

        String createBody = mockMvc.perform(post("/api/admin/notes")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        NoteResponse created = mapper.readValue(createBody, NoteResponse.class);
        NoteRequest updateRequest = new NoteRequest("Revised and pinned note", LocalDate.now(), true, studentId);

        mockMvc.perform(put("/api/admin/notes/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("Revised and pinned note"))
            .andExpect(jsonPath("$.pinned").value(true));
    }

    @Test
    @DisplayName("A deleted note can no longer be found")
    void testDelete_ThenGetReturnsNotFound() throws Exception {
        Long studentId = createStudent();
        NoteRequest createRequest = new NoteRequest("Temporary note", LocalDate.now(), false, studentId);

        String createBody = mockMvc.perform(post("/api/admin/notes")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        NoteResponse created = mapper.readValue(createBody, NoteResponse.class);

        mockMvc.perform(delete("/api/admin/notes/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/notes/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Creating a note with a missing required field is rejected")
    void testCreate_MissingRequiredField_ReturnsBadRequest() throws Exception {
        String invalidJson = "{\"date\": \"2026-09-15\", \"pinned\": false}";

        mockMvc.perform(post("/api/admin/notes")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }
}
