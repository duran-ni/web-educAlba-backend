package dev.duran.web_educAlba_backend.academy;

import dev.duran.web_educAlba_backend.academy.enrollment.PublicEnrollmentRequest;
import dev.duran.web_educAlba_backend.academy.workshop.WorkshopRequest;
import dev.duran.web_educAlba_backend.academy.workshop.WorkshopResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class DashboardControllerTest {

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
    @DisplayName("The summary reflects active workshops and distinct enrolled students")
    void testGetSummary_ReflectsActiveWorkshopsAndDistinctEnrolledStudents() throws Exception {
        // Se compara el resumen antes y despues, en vez de un numero fijo,
        // porque la base de datos de test es compartida con el resto de tests
        // y puede contener ya datos de otras clases
        DashboardSummaryResponse before = fetchSummary();

        Long activeWorkshopId1 = createWorkshop(true);
        Long activeWorkshopId2 = createWorkshop(true);
        createWorkshop(false);

        // Un alumno apuntado a dos talleres a la vez: debe contar como un solo alumno
        enrollPublicly("Lucía García", List.of(activeWorkshopId1, activeWorkshopId2));
        // Un segundo alumno distinto, apuntado a un solo taller
        enrollPublicly("Marcos Ruiz", List.of(activeWorkshopId1));

        DashboardSummaryResponse after = fetchSummary();

        assertThat(after.totalActiveWorkshops() - before.totalActiveWorkshops()).isEqualTo(2);
        assertThat(after.totalEnrolledStudents() - before.totalEnrolledStudents()).isEqualTo(2);
    }

    private DashboardSummaryResponse fetchSummary() throws Exception {
        String body = mockMvc.perform(get("/api/admin/dashboard/summary")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return mapper.readValue(body, DashboardSummaryResponse.class);
    }

    private Long createWorkshop(boolean active) throws Exception {
        WorkshopRequest request = new WorkshopRequest("Taller de prueba", "Descripción", LocalDate.now().plusDays(10),
                LocalTime.of(10, 0), "6-12", "Sala A", active);

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

    private void enrollPublicly(String studentName, List<Long> workshopIds) throws Exception {
        PublicEnrollmentRequest request = new PublicEnrollmentRequest(studentName, 10, "612345678", workshopIds);

        mockMvc.perform(post("/api/public/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }
}
