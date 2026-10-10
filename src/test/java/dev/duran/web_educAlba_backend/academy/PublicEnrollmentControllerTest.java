package dev.duran.web_educAlba_backend.academy;

import dev.duran.web_educAlba_backend.academy.workshop.WorkshopRequest;
import dev.duran.web_educAlba_backend.academy.workshop.WorkshopResponse;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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
class PublicEnrollmentControllerTest {

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

    private Long createWorkshop(String name) throws Exception {
        WorkshopRequest request = new WorkshopRequest(name, "Description", LocalDate.now().plusDays(7), LocalTime.of(16, 0), "8-12", "Room A", true);

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
    @DisplayName("A visitor can enroll in several workshops at once without authentication")
    void testCreate_ReturnsCreated() throws Exception {
        Long ceramicsId = createWorkshop("Ceramics");
        Long roboticsId = createWorkshop("Robotics");
        PublicEnrollmentRequest request = new PublicEnrollmentRequest("Ana García", 8, "612345678", List.of(ceramicsId, roboticsId));

        mockMvc.perform(post("/api/public/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].workshopName").value("Ceramics"))
            .andExpect(jsonPath("$[1].workshopName").value("Robotics"));
    }

    @Test
    @DisplayName("Submitting the form with a missing required field is rejected")
    void testCreate_MissingRequiredField_ReturnsBadRequest() throws Exception {
        String invalidJson = "{\"age\": 8, \"phone\": \"612345678\", \"workshopIds\": [1]}";

        mockMvc.perform(post("/api/public/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Submitting the form with an invalid phone format is rejected")
    void testCreate_InvalidPhone_ReturnsBadRequest() throws Exception {
        Long workshopId = createWorkshop("Painting");
        PublicEnrollmentRequest request = new PublicEnrollmentRequest("Ana García", 8, "123", List.of(workshopId));

        mockMvc.perform(post("/api/public/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Enrolling in a workshop that does not exist is rejected")
    void testCreate_UnknownWorkshop_ReturnsNotFound() throws Exception {
        PublicEnrollmentRequest request = new PublicEnrollmentRequest("Ana García", 8, "612345678", List.of(999999L));

        mockMvc.perform(post("/api/public/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }
}
