package dev.duran.web_educAlba_backend.academy.student;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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

        email = "dashboard" + System.nanoTime() + "@example.com";

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
            .price(BigDecimal.valueOf(30))
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
            .andExpect(jsonPath("$[0].progress").value("iniciado"))
            .andExpect(jsonPath("$[0].price").value(30));
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
    @DisplayName("A logged in user enrolls their own student in an active upcoming workshop")
    void testEnrollInWorkshop_ReturnsCreated() throws Exception {
        Workshop workshop = workshopRepository.save(Workshop.builder()
            .name("Robótica")
            .date(LocalDate.now().plusDays(3))
            .time(LocalTime.of(17, 0))
            .active(true)
            .price(BigDecimal.valueOf(30))
            .build());

        mockMvc.perform(post("/api/dashboard/my-workshops/" + workshop.getId())
                .with(httpBasic(email, PLAIN_PASSWORD)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.workshopName").value("Robótica"));

        Long userId = userRepository.findByEmail(email).orElseThrow().getId();
        Student student = studentRepository.findByUser_Id(userId).orElseThrow();
        assertThat(enrollmentRepository.existsByStudentIdAndWorkshopId(student.getId(), workshop.getId())).isTrue();
    }

    @Test
    @DisplayName("Enrolling in a workshop that does not exist returns not found")
    void testEnrollInWorkshop_WorkshopNotFound_ReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/dashboard/my-workshops/999999")
                .with(httpBasic(email, PLAIN_PASSWORD)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Enrolling in a workshop whose date has already passed returns not found")
    void testEnrollInWorkshop_PastWorkshop_ReturnsNotFound() throws Exception {
        Workshop workshop = workshopRepository.save(Workshop.builder()
            .name("Ya pasado")
            .date(LocalDate.now().minusDays(1))
            .active(true)
            .build());

        mockMvc.perform(post("/api/dashboard/my-workshops/" + workshop.getId())
                .with(httpBasic(email, PLAIN_PASSWORD)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Enrolling twice in the same workshop returns conflict")
    void testEnrollInWorkshop_AlreadyEnrolled_ReturnsConflict() throws Exception {
        Workshop workshop = workshopRepository.save(Workshop.builder()
            .name("Robótica")
            .date(LocalDate.now().plusDays(3))
            .active(true)
            .build());

        mockMvc.perform(post("/api/dashboard/my-workshops/" + workshop.getId())
                .with(httpBasic(email, PLAIN_PASSWORD)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/dashboard/my-workshops/" + workshop.getId())
                .with(httpBasic(email, PLAIN_PASSWORD)))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("An unauthenticated request to the dashboard is rejected")
    void testGetMyProfile_NoAuth_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/dashboard/me"))
            .andExpect(status().isUnauthorized());
    }
}
