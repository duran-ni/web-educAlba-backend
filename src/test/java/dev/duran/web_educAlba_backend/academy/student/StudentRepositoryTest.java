package dev.duran.web_educAlba_backend.academy.student;

import dev.duran.web_educAlba_backend.user.core.UserEntity;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

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
class StudentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StudentRepository studentRepository;

    @Test
    void findsActiveStudentsOrderedByIdDescending() {
        Student pending = Student.builder().firstName("Lucía").lastName("García").status(StudentStatus.PENDING).build();
        Student active1 = Student.builder().firstName("Marcos").lastName("Ruiz").status(StudentStatus.ACTIVE).build();
        Student active2 = Student.builder().firstName("Elena").lastName("Pérez").status(StudentStatus.ACTIVE).build();
        entityManager.persistAndFlush(pending);
        entityManager.persistAndFlush(active1);
        entityManager.persistAndFlush(active2);

        List<Student> result = studentRepository.findByStatusOrderByIdDesc(StudentStatus.ACTIVE);

        // El listado de "ultimos alumnos" debe mostrar primero el mas reciente
        assertThat(result).containsExactly(active2, active1);
    }

    @Test
    void findsStudentByLinkedUserId() {
        UserEntity user = UserEntity.builder().email("family@educalba.com").password("hashed-password").build();
        entityManager.persistAndFlush(user);

        Student student = Student.builder().firstName("Lucía").lastName("García").user(user).build();
        entityManager.persistAndFlush(student);

        Optional<Student> result = studentRepository.findByUser_Id(user.getId());

        assertThat(result).contains(student);
    }

    @Test
    void returnsEmptyWhenNoStudentIsLinkedToThatUser() {
        Optional<Student> result = studentRepository.findByUser_Id(999L);

        assertThat(result).isEmpty();
    }
}
