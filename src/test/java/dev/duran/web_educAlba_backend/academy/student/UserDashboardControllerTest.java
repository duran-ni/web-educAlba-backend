package dev.duran.web_educAlba_backend.academy.student;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;

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
import dev.duran.web_educAlba_backend.academy.enrollment.Enrollment;
import dev.duran.web_educAlba_backend.academy.enrollment.EnrollmentRepository;
import dev.duran.web_educAlba_backend.academy.workshop.Workshop;
import dev.duran.web_educAlba_backend.academy.workshop.WorkshopRepository;
import dev.duran.web_educAlba_backend.user.core.UserRepository;
import dev.duran.web_educAlba_backend.user.register.RegisterRequest;
import tools.jackson.databind.ObjectMapper;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserDashboardControllerTest {

    private static final String PLAIN_PASSWORD = "ClaveSegura123";

    @Autowired
    WebApplicationContext context;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    WorkshopRepository workshopRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    MaterialRepository materialRepository;

    MockMvc mockMvc;

    // Email distinto en cada test: si fuera un valor fijo, el segundo test
    // que se ejecutase fallaria el registro con un 409 (email duplicado)
    String email;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(SecurityMockMvcConfigurers.springSecurity())
            .build();

        email = "dashboard" + System.nanoTime() + "@educalba.com";

        // RegisterService ya crea el UserEntity con rol FAMILY y el Student
        // (ACTIVE) vinculado a el en la misma transaccion -- no hace falta
        // montarlos a mano con los repositorios
        String encodedPassword = Base64.getEncoder().encodeToString(PLAIN_PASSWORD.getBytes());
        RegisterRequest request = new RegisterRequest("Lucía", "García", email, encodedPassword,
            "Clases de Refuerzo", EducationalStage.PRIMARIA);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("A logged in user gets the profile of their own linked student")
    void testGetMyProfile_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/dashboard/me")
                .with(httpBasic(email, PLAIN_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Lucía"))
            .andExpect(jsonPath("$.lastName").value("García"));
    }

    @Test
    @DisplayName("A logged in user with no enrollments gets an empty workshop list")
    void testGetMyWorkshops_NoEnrollments_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/dashboard/my-workshops")
                .with(httpBasic(email, PLAIN_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("A logged in user gets the workshops their own student is enrolled in")
    void testGetMyWorkshops_ReturnsOk() throws Exception {
        Long userId = userRepository.findByEmail(email).orElseThrow().getId();
        Student student = studentRepository.findByUser_Id(userId).orElseThrow();

        Workshop workshop = workshopRepository.save(Workshop.builder()
            .name("Robótica")
            .date(LocalDate.of(2026, 11, 10))
            .time(LocalTime.of(17, 0))
            .build());

        enrollmentRepository.save(Enrollment.builder()
            .student(student)
            .workshop(workshop)
            .progress("iniciado")
            .build());

        mockMvc.perform(get("/api/dashboard/my-workshops")
                .with(httpBasic(email, PLAIN_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].workshopName").value("Robótica"))
            .andExpect(jsonPath("$[0].progress").value("iniciado"));
    }

    @Test
    @DisplayName("A logged in user with no materials gets an empty materials list")
    void testGetMyMaterials_NoMaterials_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/dashboard/my-materials")
                .with(httpBasic(email, PLAIN_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("A logged in user gets the materials uploaded for their own student")
    void testGetMyMaterials_ReturnsOk() throws Exception {
        Long userId = userRepository.findByEmail(email).orElseThrow().getId();
        Student student = studentRepository.findByUser_Id(userId).orElseThrow();

        materialRepository.save(Material.builder()
            .title("Worksheet 1")
            .description("Ejercicios 1 y 2")
            .fileUrl("https://res.cloudinary.com/demo/raw/upload/worksheet.pdf")
            .publicId("worksheet-public-id")
            .fileSizeBytes(245_760L)
            .uploadedAt(LocalDateTime.now())
            .student(student)
            .build());

        mockMvc.perform(get("/api/dashboard/my-materials")
                .with(httpBasic(email, PLAIN_PASSWORD)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("Worksheet 1"))
            .andExpect(jsonPath("$[0].fileSizeBytes").value(245_760))
            .andExpect(jsonPath("$[0].downloadUrl").value(
                "https://res.cloudinary.com/demo/raw/upload/fl_attachment:Worksheet_1/worksheet.pdf"));
    }

    @Test
    @DisplayName("An unauthenticated request to the dashboard is rejected")
    void testGetMyProfile_NoAuth_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/dashboard/me"))
            .andExpect(status().isUnauthorized());
    }
}
