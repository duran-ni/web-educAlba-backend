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
class WorkshopControllerTest {

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
    @DisplayName("A new workshop can be created")
    void testCreate_ReturnsCreated() throws Exception {
        WorkshopRequest request = new WorkshopRequest("Robotics", "Introduction to robotics", LocalDate.now().plusDays(10), "8-12", "Room A", true);

        mockMvc.perform(post("/api/admin/workshops")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Robotics"))
            .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("All workshops can be listed")
    void testGetAll_ReturnsOk() throws Exception {
        WorkshopRequest request = new WorkshopRequest("Painting", "Watercolor basics", LocalDate.now().plusDays(5), "6-10", "Room B", true);

        mockMvc.perform(post("/api/admin/workshops")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/admin/workshops")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("An existing workshop can be updated")
    void testUpdate_ReturnsOk() throws Exception {
        WorkshopRequest createRequest = new WorkshopRequest("Music", "Guitar for beginners", LocalDate.now().plusDays(15), "10-14", "Room C", true);

        String createBody = mockMvc.perform(post("/api/admin/workshops")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        WorkshopResponse created = mapper.readValue(createBody, WorkshopResponse.class);
        WorkshopRequest updateRequest = new WorkshopRequest("Music", "Guitar and ukulele for beginners", LocalDate.now().plusDays(15), "10-14", "Room D", false);

        mockMvc.perform(put("/api/admin/workshops/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.room").value("Room D"))
            .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    @DisplayName("A deleted workshop can no longer be found")
    void testDelete_ThenGetReturnsNotFound() throws Exception {
        WorkshopRequest createRequest = new WorkshopRequest("Theatre", "Improv workshop", LocalDate.now().plusDays(20), "12-16", "Room E", true);

        String createBody = mockMvc.perform(post("/api/admin/workshops")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        WorkshopResponse created = mapper.readValue(createBody, WorkshopResponse.class);

        mockMvc.perform(delete("/api/admin/workshops/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/workshops/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Creating a workshop with a missing required field is rejected")
    void testCreate_MissingRequiredField_ReturnsBadRequest() throws Exception {
        String invalidJson = "{\"description\": \"Missing name and date\", \"room\": \"Room F\", \"active\": true}";

        mockMvc.perform(post("/api/admin/workshops")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }
}
