package dev.duran.web_educAlba_backend.academy.communication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import dev.duran.web_educAlba_backend.TestcontainersConfiguration;
import tools.jackson.databind.ObjectMapper;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InterestSubmissionAdminControllerTest {

    private static final String ADMIN_EMAIL = "admin@educalba.com";
    private static final String ADMIN_PASSWORD = "AdminEducAlba123";

    @Autowired
    WebApplicationContext context;

    @Autowired
    ObjectMapper mapper;

    // Sustituye al JavaMailSender real por un doble de prueba: crear una
    // solicitud de prueba dispara el envio de correo, y no se quiere enviar
    // emails de verdad al ejecutar los tests
    @MockitoBean
    JavaMailSender javaMailSender;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();
    }

    @Test
    @DisplayName("An admin can list all interest submissions")
    void testGetAll_ReturnsAllSubmissions() throws Exception {
        Long id = createInterestSubmission("Lucía García", "Matemáticas 2º ESO", "612345678");

        String body = mockMvc.perform(get("/api/admin/interest-submissions")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        List<InterestSubmissionResponse> submissions = mapper.readValue(
            body, mapper.getTypeFactory().constructCollectionType(List.class, InterestSubmissionResponse.class));

        assertThat(submissions).anySatisfy(submission -> {
            assertThat(submission.id()).isEqualTo(id);
            assertThat(submission.studentName()).isEqualTo("Lucía García");
            assertThat(submission.reviewed()).isFalse();
        });
    }

    @Test
    @DisplayName("An admin can mark an interest submission as reviewed")
    void testMarkReviewed_UpdatesReviewedFlag() throws Exception {
        Long id = createInterestSubmission("Marcos Ruiz", "Lengua 3º Primaria", "612345679");

        mockMvc.perform(put("/api/admin/interest-submissions/{id}/reviewed", id)
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.reviewed").value(true));
    }

    @Test
    @DisplayName("Marking an unknown interest submission as reviewed returns not found")
    void testMarkReviewed_UnknownId_ReturnsNotFound() throws Exception {
        mockMvc.perform(put("/api/admin/interest-submissions/{id}/reviewed", Long.MAX_VALUE)
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("An admin can close an interest submission")
    void testMarkClosed_UpdatesClosedFlag() throws Exception {
        Long id = createInterestSubmission("Sofía Díaz", "Inglés 4º Primaria", "612345680");

        mockMvc.perform(put("/api/admin/interest-submissions/{id}/closed", id)
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @DisplayName("Closing an unknown interest submission returns not found")
    void testMarkClosed_UnknownId_ReturnsNotFound() throws Exception {
        mockMvc.perform(put("/api/admin/interest-submissions/{id}/closed", Long.MAX_VALUE)
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("A closed interest submission no longer appears in the admin listing")
    void testGetAll_ExcludesClosedSubmissions() throws Exception {
        Long id = createInterestSubmission("Pablo Ortiz", "Física y Química 4º ESO", "612345681");

        mockMvc.perform(put("/api/admin/interest-submissions/{id}/closed", id)
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isOk());

        String body = mockMvc.perform(get("/api/admin/interest-submissions")
                .with(httpBasic(ADMIN_EMAIL, ADMIN_PASSWORD)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        List<InterestSubmissionResponse> submissions = mapper.readValue(
            body, mapper.getTypeFactory().constructCollectionType(List.class, InterestSubmissionResponse.class));

        assertThat(submissions).noneMatch(submission -> submission.id().equals(id));
    }

    private Long createInterestSubmission(String studentName, String courseToReinforce, String phone) throws Exception {
        InterestSubmissionRequest request = new InterestSubmissionRequest(studentName, courseToReinforce, phone);

        String body = mockMvc.perform(post("/api/public/interest-submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

        return mapper.readValue(body, InterestSubmissionResponse.class).id();
    }
}
