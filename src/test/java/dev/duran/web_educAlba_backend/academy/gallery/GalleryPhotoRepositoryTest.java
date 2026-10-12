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
class GalleryPhotoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private GalleryPhotoRepository galleryPhotoRepository;

    @Test
    void findsAllPhotosOrderedByUploadDateDescending() {
        GalleryPhoto oldest = GalleryPhoto.builder()
                .imageUrl("https://res.cloudinary.com/demo/old.jpg")
                .publicId("old")
                .uploadedAt(LocalDateTime.now().minusDays(2))
                .build();
        GalleryPhoto newest = GalleryPhoto.builder()
                .imageUrl("https://res.cloudinary.com/demo/new.jpg")
                .publicId("new")
                .uploadedAt(LocalDateTime.now())
                .build();
        entityManager.persistAndFlush(oldest);
        entityManager.persistAndFlush(newest);

        List<GalleryPhoto> result = galleryPhotoRepository.findAllByOrderByUploadedAtDesc();

        // La foto mas reciente debe aparecer primero en la lista
        assertThat(result).containsExactly(newest, oldest);
    }
}
