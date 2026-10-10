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
import dev.duran.web_educAlba_backend.user.UserEntity;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NoteRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NoteRepository noteRepository;

    @Test
    void findsAllNotesAndOnlyThePinnedOnesOfAStudent() {
        Student student = Student.builder().firstName("Lucía").build();
        UserEntity author = UserEntity.builder().email("admin@educalba.com").password("hashed-password").build();
        entityManager.persistAndFlush(student);
        entityManager.persistAndFlush(author);

        Note pinned = Note.builder()
            .content("Buen trabajo esta semana")
            .date(LocalDate.now())
            .pinned(true)
            .student(student)
            .author(author)
            .build();
        Note regular = Note.builder()
            .content("Nota personal de la familia")
            .date(LocalDate.now())
            .pinned(false)
            .student(student)
            .author(author)
            .build();
        entityManager.persistAndFlush(pinned);
        entityManager.persistAndFlush(regular);

        List<Note> allNotes = noteRepository.findByStudentId(student.getId());
        List<Note> pinnedNotes = noteRepository.findByStudentIdAndPinnedTrue(student.getId());

        assertThat(allNotes).containsExactlyInAnyOrder(pinned, regular);
        assertThat(pinnedNotes).containsExactly(pinned);
    }
}
