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
class EnrollmentControllerTest {

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
        StudentRequest request = new StudentRequest("Elena", "Torres", "Refuerzo", EducationalStage.PRIMARIA);

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

    private Long createWorkshop() throws Exception {
        WorkshopRequest request = new WorkshopRequest("Ceramics", "Introduction to ceramics", LocalDate.now().plusDays(7), "8-12", "Room A", true);

        String body = mockMvc.perform(post("/api/admin/workshops")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return mapper.readValue(body, WorkshopResponse.class).id();
    }

    @Test
    @DisplayName("A new enrollment can be created")
    void testCreate_ReturnsCreated() throws Exception {
        Long studentId = createStudent();
        Long workshopId = createWorkshop();
        EnrollmentRequest request = new EnrollmentRequest(studentId, workshopId, LocalDate.now(), "Just started");

        mockMvc.perform(post("/api/admin/enrollments")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.studentFullName").value("Elena Torres"))
            .andExpect(jsonPath("$.workshopName").value("Ceramics"));
    }

    @Test
    @DisplayName("All enrollments can be listed")
    void testGetAll_ReturnsOk() throws Exception {
        Long studentId = createStudent();
        Long workshopId = createWorkshop();
        EnrollmentRequest request = new EnrollmentRequest(studentId, workshopId, LocalDate.now(), "Just started");

        mockMvc.perform(post("/api/admin/enrollments")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/admin/enrollments")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("An existing enrollment can be updated")
    void testUpdate_ReturnsOk() throws Exception {
        Long studentId = createStudent();
        Long workshopId = createWorkshop();
        EnrollmentRequest createRequest = new EnrollmentRequest(studentId, workshopId, LocalDate.now(), "Just started");

        String createBody = mockMvc.perform(post("/api/admin/enrollments")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        EnrollmentResponse created = mapper.readValue(createBody, EnrollmentResponse.class);
        EnrollmentRequest updateRequest = new EnrollmentRequest(studentId, workshopId, LocalDate.now(), "Halfway through");

        mockMvc.perform(put("/api/admin/enrollments/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.progress").value("Halfway through"));
    }

    @Test
    @DisplayName("A deleted enrollment can no longer be found")
    void testDelete_ThenGetReturnsNotFound() throws Exception {
        Long studentId = createStudent();
        Long workshopId = createWorkshop();
        EnrollmentRequest createRequest = new EnrollmentRequest(studentId, workshopId, LocalDate.now(), "Just started");

        String createBody = mockMvc.perform(post("/api/admin/enrollments")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        EnrollmentResponse created = mapper.readValue(createBody, EnrollmentResponse.class);

        mockMvc.perform(delete("/api/admin/enrollments/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/enrollments/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Creating an enrollment with a missing required field is rejected")
    void testCreate_MissingRequiredField_ReturnsBadRequest() throws Exception {
        String invalidJson = "{\"progress\": \"Missing studentId and workshopId\"}";

        mockMvc.perform(post("/api/admin/enrollments")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }
}
