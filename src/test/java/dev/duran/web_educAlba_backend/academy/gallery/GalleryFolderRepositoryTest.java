package dev.duran.web_educAlba_backend.academy.gallery;

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
class GalleryFolderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GalleryFolderRepository galleryFolderRepository;

    @Test
    void findsAllFoldersOrderedByCreationAscending() {
        GalleryFolder first = GalleryFolder.builder()
                .name("Nuestra academia")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        GalleryFolder second = GalleryFolder.builder()
                .name("Breakout Halloween")
                .createdAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(first);
        entityManager.persistAndFlush(second);

        List<GalleryFolder> result = galleryFolderRepository.findAllByOrderByCreatedAtAsc();

        // La carpeta creada antes debe aparecer primero
        assertThat(result).containsExactly(first, second);
    }
}
