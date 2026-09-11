package dev.duran.web_educAlba_backend.academy;

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
class MaterialRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MaterialRepository materialRepository;

    @Test
    void findsMaterialsOfAStudent() {
        Student student = Student.builder().firstName("Lucía").build();
        entityManager.persistAndFlush(student);

        Material material = Material.builder()
            .name("Ficha de refuerzo - Sumas")
            .subject("Matemáticas")
            .fileSize(1024L)
            .filePath("/materiales/sumas.pdf")
            .student(student)
            .build();
        entityManager.persistAndFlush(material);

        List<Material> result = materialRepository.findByStudentId(student.getId());

        assertThat(result).containsExactly(material);
    }
}
