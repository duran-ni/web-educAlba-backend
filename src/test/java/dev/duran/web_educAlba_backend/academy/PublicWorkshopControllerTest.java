package dev.duran.web_educAlba_backend.academy;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import dev.duran.web_educAlba_backend.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PublicWorkshopControllerTest {

    @Autowired
    WebApplicationContext context;

    @Autowired
    WorkshopRepository workshopRepository;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();

        // Desactiva (no borra, para no violar la clave foránea de agenda_events)
        // cualquier taller activo que hayan dejado otras clases de test, para que
        // el escenario "sin talleres próximos" sea fiable
        List<Workshop> existingWorkshops = workshopRepository.findAll();
        existingWorkshops.forEach(workshop -> workshop.setActive(false));
        workshopRepository.saveAll(existingWorkshops);
    }

    @Test
    @DisplayName("A visitor can see the next upcoming workshop without authentication")
    void testGetNext_ReturnsOk() throws Exception {
        workshopRepository.save(Workshop.builder()
            .name("Robótica Creativa")
            .date(LocalDate.now().plusDays(5))
            .active(true)
            .build());

        mockMvc.perform(get("/api/public/workshops/next"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Robótica Creativa"));
    }

    @Test
    @DisplayName("No content is returned when there are no upcoming workshops")
    void testGetNext_NoUpcomingWorkshops_ReturnsNoContent() throws Exception {
        mockMvc.perform(get("/api/public/workshops/next"))
            .andExpect(status().isNoContent());
    }
}
