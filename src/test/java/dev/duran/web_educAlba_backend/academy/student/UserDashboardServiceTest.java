package dev.duran.web_educAlba_backend.academy.student;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.duran.web_educAlba_backend.academy.enrollment.Enrollment;
import dev.duran.web_educAlba_backend.academy.enrollment.EnrollmentRepository;
import dev.duran.web_educAlba_backend.academy.workshop.Workshop;

@ExtendWith(MockitoExtension.class)
class UserDashboardServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Test
    void returnsTheProfileOfTheStudentLinkedToTheUser() {
        Student student = Student.builder().id(1L).firstName("Lucía").lastName("García").build();
        when(studentRepository.findByUser_Id(10L)).thenReturn(Optional.of(student));

        UserDashboardService service = new UserDashboardService(studentRepository, enrollmentRepository);
        StudentResponse result = service.getMyProfile(10L);

        assertThat(result.firstName()).isEqualTo("Lucía");
    }

    @Test
    void throwsWhenNoStudentIsLinkedToTheUser() {
        when(studentRepository.findByUser_Id(10L)).thenReturn(Optional.empty());

        UserDashboardService service = new UserDashboardService(studentRepository, enrollmentRepository);

        assertThatThrownBy(() -> service.getMyProfile(10L))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsTheWorkshopsTheStudentIsEnrolledIn() {
        Student student = Student.builder().id(1L).firstName("Lucía").build();
        Workshop workshop = Workshop.builder().id(5L).name("Robótica").date(LocalDate.of(2026, 11, 10)).time(LocalTime.of(17, 0)).build();
        Enrollment enrollment = Enrollment.builder().id(20L).student(student).workshop(workshop).progress("iniciado").build();

        when(studentRepository.findByUser_Id(10L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of(enrollment));

        UserDashboardService service = new UserDashboardService(studentRepository, enrollmentRepository);
        List<MyWorkshopResponse> result = service.getMyWorkshops(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).workshopName()).isEqualTo("Robótica");
        assertThat(result.get(0).progress()).isEqualTo("iniciado");
    }

    @Test
    void returnsAnEmptyListWhenTheStudentHasNoEnrollments() {
        Student student = Student.builder().id(1L).firstName("Lucía").build();
        when(studentRepository.findByUser_Id(10L)).thenReturn(Optional.of(student));
        when(enrollmentRepository.findByStudentId(1L)).thenReturn(List.of());

        UserDashboardService service = new UserDashboardService(studentRepository, enrollmentRepository);
        List<MyWorkshopResponse> result = service.getMyWorkshops(10L);

        assertThat(result).isEmpty();
    }
}
