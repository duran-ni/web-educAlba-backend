package dev.duran.web_educAlba_backend.academy;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

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
class AnnouncementRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Test
    void savesAndRetrievesAnnouncementWithItsAuthor() {
        UserEntity author = UserEntity.builder().email("admin@educalba.com").password("hashed-password").build();
        entityManager.persistAndFlush(author);

        Announcement announcement = Announcement.builder()
            .title("Cierre por vacaciones")
            .content("La academia permanecerá cerrada del 1 al 15 de agosto")
            .date(LocalDate.now())
            .author(author)
            .build();
        Long savedId = announcementRepository.save(announcement).getId();
        entityManager.flush();
        entityManager.clear();

        Announcement found = announcementRepository.findById(savedId).orElseThrow();

        assertThat(found.getTitle()).isEqualTo("Cierre por vacaciones");
        assertThat(found.getAuthor().getId()).isEqualTo(author.getId());
    }
}
