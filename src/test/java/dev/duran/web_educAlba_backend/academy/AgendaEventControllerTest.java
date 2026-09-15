package dev.duran.web_educAlba_backend.academy;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;

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
class AgendaEventControllerTest {

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

    private Long createWorkshop() throws Exception {
        WorkshopRequest request = new WorkshopRequest("Chess Club", "Weekly chess sessions", LocalDate.now().plusDays(3), "10-16", "Room B", true);

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
    @DisplayName("A new agenda event linked to a workshop can be created")
    void testCreate_ReturnsCreated() throws Exception {
        Long workshopId = createWorkshop();
        AgendaEventRequest request = new AgendaEventRequest(LocalDate.now(), LocalTime.of(10, 0), "Chess tournament", "Room B", workshopId);

        mockMvc.perform(post("/api/admin/agenda-events")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.activity").value("Chess tournament"))
            .andExpect(jsonPath("$.workshopName").value("Chess Club"));
    }

    @Test
    @DisplayName("A new agenda event without a workshop can be created")
    void testCreate_WithoutWorkshop_ReturnsCreatedWithNullWorkshop() throws Exception {
        AgendaEventRequest request = new AgendaEventRequest(LocalDate.now(), LocalTime.of(12, 30), "General staff meeting", "Main hall", null);

        mockMvc.perform(post("/api/admin/agenda-events")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.activity").value("General staff meeting"))
            .andExpect(jsonPath("$.workshopId").doesNotExist())
            .andExpect(jsonPath("$.workshopName").doesNotExist());
    }

    @Test
    @DisplayName("All agenda events can be listed")
    void testGetAll_ReturnsOk() throws Exception {
        AgendaEventRequest request = new AgendaEventRequest(LocalDate.now(), LocalTime.of(9, 0), "Morning briefing", "Main hall", null);

        mockMvc.perform(post("/api/admin/agenda-events")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/admin/agenda-events")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("An existing agenda event can be updated")
    void testUpdate_ReturnsOk() throws Exception {
        AgendaEventRequest createRequest = new AgendaEventRequest(LocalDate.now(), LocalTime.of(11, 0), "Draft session", "Room C", null);

        String createBody = mockMvc.perform(post("/api/admin/agenda-events")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        AgendaEventResponse created = mapper.readValue(createBody, AgendaEventResponse.class);
        AgendaEventRequest updateRequest = new AgendaEventRequest(LocalDate.now(), LocalTime.of(11, 30), "Confirmed session", "Room D", null);

        mockMvc.perform(put("/api/admin/agenda-events/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.activity").value("Confirmed session"))
            .andExpect(jsonPath("$.room").value("Room D"));
    }

    @Test
    @DisplayName("A deleted agenda event can no longer be found")
    void testDelete_ThenGetReturnsNotFound() throws Exception {
        AgendaEventRequest createRequest = new AgendaEventRequest(LocalDate.now(), LocalTime.of(15, 0), "Temporary event", "Room E", null);

        String createBody = mockMvc.perform(post("/api/admin/agenda-events")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createRequest)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        AgendaEventResponse created = mapper.readValue(createBody, AgendaEventResponse.class);

        mockMvc.perform(delete("/api/admin/agenda-events/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/agenda-events/" + created.id())
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Creating an agenda event with a missing required field is rejected")
    void testCreate_MissingRequiredField_ReturnsBadRequest() throws Exception {
        String invalidJson = "{\"room\": \"Room F\"}";

        mockMvc.perform(post("/api/admin/agenda-events")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD))
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }
}
