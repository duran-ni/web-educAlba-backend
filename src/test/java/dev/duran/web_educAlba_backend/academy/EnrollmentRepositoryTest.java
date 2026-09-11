package dev.duran.web_educAlba_backend.academy;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import dev.duran.web_educAlba_backend.TestcontainersConfiguration;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EnrollmentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Test
    void findsEnrollmentsOfAStudent() {
        Student student = Student.builder().firstName("Lucía").build();
        Workshop workshop = Workshop.builder().name("Robótica").date(LocalDate.now()).build();
        entityManager.persistAndFlush(student);
        entityManager.persistAndFlush(workshop);

        Enrollment enrollment = Enrollment.builder()
            .student(student)
            .workshop(workshop)
            .enrollmentDate(LocalDate.now())
            .progress("iniciado")
            .build();
        entityManager.persistAndFlush(enrollment);

        List<Enrollment> result = enrollmentRepository.findByStudentId(student.getId());

        assertThat(result).containsExactly(enrollment);
    }
}
