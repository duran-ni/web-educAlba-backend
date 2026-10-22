package dev.duran.web_educAlba_backend.academy.student;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
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
class MaterialRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MaterialRepository materialRepository;

    @Test
    void findsMaterialsOfAStudent_OrderedFromMostRecent() {
        Student student = Student.builder().firstName("Lucía").build();
        entityManager.persistAndFlush(student);

        Material olderMaterial = Material.builder()
            .title("Ficha de refuerzo - Sumas")
            .description("Resuelve los ejercicios 1 al 5")
            .fileUrl("https://res.cloudinary.com/demo/sumas.pdf")
            .publicId("sumas-public-id")
            .uploadedAt(LocalDateTime.now().minusDays(1))
            .student(student)
            .build();
        entityManager.persistAndFlush(olderMaterial);

        Material newerMaterial = Material.builder()
            .title("Ficha de refuerzo - Restas")
            .fileUrl("https://res.cloudinary.com/demo/restas.pdf")
            .publicId("restas-public-id")
            .uploadedAt(LocalDateTime.now())
            .student(student)
            .build();
        entityManager.persistAndFlush(newerMaterial);

        List<Material> result = materialRepository.findByStudentIdOrderByUploadedAtDesc(student.getId());

        assertThat(result).containsExactly(newerMaterial, olderMaterial);
    }
}
