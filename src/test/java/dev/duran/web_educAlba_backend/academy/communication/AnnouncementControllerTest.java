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
class AnnouncementControllerTest {

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
    @DisplayName("A new announcement can be created")
    void testCreate_ReturnsCreated() throws Exception {
        AnnouncementRequest request = new AnnouncementRequest("Autumn break schedule", "The center will be closed from Oct 12 to Oct 16", LocalDate.now());

        mockMvc.perform(post("/api/admin/announcements")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Autumn break schedule"))
            .andExpect(jsonPath("$.authorEmail").value(ADMIN_EMAIL));
    }

    @Test
    @DisplayName("All announcements can be listed")
    void testGetAll_ReturnsOk() throws Exception {
        AnnouncementRequest request = new AnnouncementRequest("New workshop schedule", "Updated timetable for next term", LocalDate.now());

        mockMvc.perform(post("/api/admin/announcements")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/admin/announcements")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("An existing announcement can be updated")
    void testUpdate_ReturnsOk() throws Exception {
        AnnouncementRequest createRequest = new AnnouncementRequest("Draft announcement", "Content pending review", LocalDate.now());

        String createBody = mockMvc.perform(post("/api/admin/announcements")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        AnnouncementResponse created = mapper.readValue(createBody, AnnouncementResponse.class);
        AnnouncementRequest updateRequest = new AnnouncementRequest("Published announcement", "Final reviewed content", LocalDate.now());

        mockMvc.perform(put("/api/admin/announcements/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Published announcement"));
    }

    @Test
    @DisplayName("A deleted announcement can no longer be found")
    void testDelete_ThenGetReturnsNotFound() throws Exception {
        AnnouncementRequest createRequest = new AnnouncementRequest("Temporary announcement", "To be removed", LocalDate.now());

        String createBody = mockMvc.perform(post("/api/admin/announcements")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        AnnouncementResponse created = mapper.readValue(createBody, AnnouncementResponse.class);

        mockMvc.perform(delete("/api/admin/announcements/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/announcements/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Creating an announcement with a missing required field is rejected")
    void testCreate_MissingRequiredField_ReturnsBadRequest() throws Exception {
        String invalidJson = "{\"content\": \"Missing title and date\"}";

        mockMvc.perform(post("/api/admin/announcements")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }
}
