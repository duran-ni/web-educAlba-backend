package dev.duran.web_educAlba_backend.academy.student;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import dev.duran.web_educAlba_backend.academy.dashboard.DashboardEventsService;
import dev.duran.web_educAlba_backend.academy.enrollment.AlreadyEnrolledException;
import dev.duran.web_educAlba_backend.academy.enrollment.Enrollment;
import dev.duran.web_educAlba_backend.academy.enrollment.EnrollmentRepository;
import dev.duran.web_educAlba_backend.academy.workshop.Workshop;
import dev.duran.web_educAlba_backend.academy.workshop.WorkshopNotFoundException;
import dev.duran.web_educAlba_backend.academy.workshop.WorkshopRepository;

@ExtendWith(MockitoExtension.class)
class UserDashboardServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private WorkshopRepository workshopRepository;

    @Mock
    private MaterialService materialService;

    @Mock
    private DashboardEventsService dashboardEventsService;

    @Mock
    private JavaMailSender javaMailSender;

    private UserDashboardService newService() {
        return new UserDashboardService(studentRepository, enrollmentRepository, workshopRepository,
                materialService, dashboardEventsService, javaMailSender,
                "contacto@example.com", "no-reply@example.com");
    }

    @Test
    void returnsTheProfileOfTheStudentLinkedToTheUser() {
        Student student = Student.builder().id(1L).firstName("Lucía").lastName("García").build();
        when(studentRepository.findByUser_Id(10L)).thenReturn(Optional.of(student));

        UserDashboardService service = newService();
        StudentResponse result = service.getMyProfile(10L);

        assertThat(result.firstName()).isEqualTo("Lucía");
    }

    @Test
    void throwsWhenNoStudentIsLinkedToTheUser() {
        when(studentRepository.findByUser_Id(10L)).thenReturn(Optional.empty());

        UserDashboardService service = newService();

        assertThatThrownBy(() -> service.getMyProfile(10L))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsTheWorkshopsTheStudentIsEnrolledIn() {
        Student student = Student.builder().id(1L).firstName("Lucía").build();
        Workshop workshop = Workshop.builder().id(5L).name("Robótica").date(LocalDate.of(2026, 11, 10)).time(LocalTime.of(17, 0)).price(BigDecimal.valueOf(30)).build();
        Enrollment enrollment = Enrollment.builder().id(20L).student(student).workshop(workshop).progress("iniciado").build();

        when(studentRepository.findByUser_Id(10L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of(enrollment));

        UserDashboardService service = newService();
        List<MyWorkshopResponse> result = service.getMyWorkshops(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).workshopName()).isEqualTo("Robótica");
        assertThat(result.get(0).progress()).isEqualTo("iniciado");
        assertThat(result.get(0).price()).isEqualByComparingTo(BigDecimal.valueOf(30));
    }

    @Test
    void returnsAnEmptyListWhenTheStudentHasNoEnrollments() {
        Student student = Student.builder().id(1L).firstName("Lucía").build();
        when(studentRepository.findByUser_Id(10L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of());

        UserDashboardService service = newService();
        List<MyWorkshopResponse> result = service.getMyWorkshops(10L);

        assertThat(result).isEmpty();
    }

    @Test
    void returnsTheMaterialsOfTheStudentLinkedToTheUser() {
        Student student = Student.builder().id(1L).firstName("Lucía").build();
        MaterialResponse material = new MaterialResponse(
            100L, "Worksheet 1", "Ejercicios 1 y 2",
            "https://res.cloudinary.com/demo/raw/upload/worksheet.pdf",
            "https://res.cloudinary.com/demo/raw/upload/fl_attachment:Worksheet_1/worksheet.pdf",
            245_760L, LocalDateTime.of(2026, 10, 22, 9, 0), 1L, "Lucía García");

        when(studentRepository.findByUser_Id(10L)).thenReturn(Optional.of(student));
        when(materialService.getByStudentId(1L)).thenReturn(List.of(material));

        UserDashboardService service = newService();
        List<MaterialResponse> result = service.getMyMaterials(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Worksheet 1");
        assertThat(result.get(0).fileSizeBytes()).isEqualTo(245_760L);
    }

    @Test
    void returnsAnEmptyListWhenTheStudentHasNoMaterials() {
        Student student = Student.builder().id(1L).firstName("Lucía").build();
        when(studentRepository.findByUser_Id(10L)).thenReturn(Optional.of(student));
        when(materialService.getByStudentId(1L)).thenReturn(List.of());

        UserDashboardService service = newService();
        List<MaterialResponse> result = service.getMyMaterials(10L);

        assertThat(result).isEmpty();
    }

    @Test
    void enrollsTheLinkedStudentInAnActiveUpcomingWorkshop() {
        Student student = Student.builder().id(1L).firstName("Lucía").build();
        Workshop workshop = Workshop.builder().id(5L).name("Robótica")
            .date(LocalDate.now().plusDays(3)).time(LocalTime.of(17, 0)).active(true)
            .price(BigDecimal.valueOf(30)).build();

        when(studentRepository.findByUser_Id(10L)).thenReturn(Optional.of(student));
        when(workshopRepository.findById(5L)).thenReturn(Optional.of(workshop));
        when(enrollmentRepository.existsByStudentIdAndWorkshopId(1L, 5L)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenAnswer(invocation -> {
            Enrollment enrollment = invocation.getArgument(0);
            enrollment.setId(99L);
            return enrollment;
        });

        UserDashboardService service = newService();
        MyWorkshopResponse result = service.enrollInWorkshop(10L, 5L);

        assertThat(result.enrollmentId()).isEqualTo(99L);
        assertThat(result.workshopName()).isEqualTo("Robótica");
        verify(dashboardEventsService).notifyChange();
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void throwsWhenTheWorkshopDoesNotExist() {
        Student student = Student.builder().id(1L).firstName("Lucía").build();
        when(studentRepository.findByUser_Id(10L)).thenReturn(Optional.of(student));
        when(workshopRepository.findById(5L)).thenReturn(Optional.empty());

        UserDashboardService service = newService();

        assertThatThrownBy(() -> service.enrollInWorkshop(10L, 5L))
            .isInstanceOf(WorkshopNotFoundException.class);
        verify(enrollmentRepository, never()).save(any());
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void throwsWhenTheWorkshopIsInactive() {
        Student student = Student.builder().id(1L).firstName("Lucía").build();
        Workshop workshop = Workshop.builder().id(5L).name("Robótica")
            .date(LocalDate.now().plusDays(3)).active(false).build();

        when(studentRepository.findByUser_Id(10L)).thenReturn(Optional.of(student));
        when(workshopRepository.findById(5L)).thenReturn(Optional.of(workshop));

        UserDashboardService service = newService();

        assertThatThrownBy(() -> service.enrollInWorkshop(10L, 5L))
            .isInstanceOf(WorkshopNotFoundException.class);
        verify(enrollmentRepository, never()).save(any());
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void throwsWhenTheWorkshopDateHasAlreadyPassed() {
        Student student = Student.builder().id(1L).firstName("Lucía").build();
        Workshop workshop = Workshop.builder().id(5L).name("Robótica")
            .date(LocalDate.now().minusDays(1)).active(true).build();

        when(studentRepository.findByUser_Id(10L)).thenReturn(Optional.of(student));
        when(workshopRepository.findById(5L)).thenReturn(Optional.of(workshop));

        UserDashboardService service = newService();

        assertThatThrownBy(() -> service.enrollInWorkshop(10L, 5L))
            .isInstanceOf(WorkshopNotFoundException.class);
        verify(enrollmentRepository, never()).save(any());
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void throwsWhenTheStudentIsAlreadyEnrolledInTheWorkshop() {
        Student student = Student.builder().id(1L).firstName("Lucía").build();
        Workshop workshop = Workshop.builder().id(5L).name("Robótica")
            .date(LocalDate.now().plusDays(3)).active(true).build();

        when(studentRepository.findByUser_Id(10L)).thenReturn(Optional.of(student));
        when(workshopRepository.findById(5L)).thenReturn(Optional.of(workshop));
        when(enrollmentRepository.existsByStudentIdAndWorkshopId(1L, 5L)).thenReturn(true);

        UserDashboardService service = newService();

        assertThatThrownBy(() -> service.enrollInWorkshop(10L, 5L))
            .isInstanceOf(AlreadyEnrolledException.class);
        verify(enrollmentRepository, never()).save(any());
        verify(javaMailSender, never()).send(any(SimpleMailMessage.class));
    }
}
