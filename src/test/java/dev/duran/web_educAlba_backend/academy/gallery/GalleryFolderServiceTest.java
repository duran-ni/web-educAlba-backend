package dev.duran.web_educAlba_backend.academy.gallery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class GalleryFolderServiceTest {

    @Mock
    GalleryFolderRepository galleryFolderRepository;

    @Mock
    GalleryPhotoRepository galleryPhotoRepository;

    GalleryFolderService galleryFolderService;

    @BeforeEach
    void setUp() {
        galleryFolderService = new GalleryFolderService(galleryFolderRepository, galleryPhotoRepository);
    }

    private GalleryFolder buildFolder(Long id) {
        return GalleryFolder.builder()
                .id(id)
                .name("Taller de pintura")
                .createdAt(LocalDateTime.now())
                .build();
    }

    private GalleryPhoto buildPhoto(Long id, GalleryFolder folder) {
        return GalleryPhoto.builder()
                .id(id)
                .imageUrl("https://res.cloudinary.com/demo/" + id + ".jpg")
                .caption("Foto " + id)
                .uploadedAt(LocalDateTime.now())
                .folder(folder)
                .build();
    }

    @Test
    @DisplayName("getAllWithPhotos returns hasMore true when the repository reports a next page")
    void getAllWithPhotos_ReturnsHasMoreTrue_WhenRepositoryHasNextPage() {
        GalleryFolder folder = buildFolder(1L);
        GalleryPhoto photo = buildPhoto(10L, folder);
        Page<GalleryFolder> page = new PageImpl<>(List.of(folder), PageRequest.of(0, 1), 2);

        when(galleryFolderRepository.findAllByOrderByCreatedAtAsc(any(Pageable.class))).thenReturn(page);
        when(galleryPhotoRepository.findByFolderIdOrderByUploadedAtDesc(1L)).thenReturn(List.of(photo));

        GalleryFolderPageResponse result = galleryFolderService.getAllWithPhotos(0, 1);

        assertThat(result.hasMore()).isTrue();
        assertThat(result.folders()).hasSize(1);
        assertThat(result.folders().get(0).photos()).hasSize(1);
    }

    @Test
    @DisplayName("getAllWithPhotos returns hasMore false when the repository reports no next page")
    void getAllWithPhotos_ReturnsHasMoreFalse_WhenRepositoryHasNoNextPage() {
        GalleryFolder folder = buildFolder(1L);
        Page<GalleryFolder> page = new PageImpl<>(List.of(folder), PageRequest.of(0, 10), 1);

        when(galleryFolderRepository.findAllByOrderByCreatedAtAsc(any(Pageable.class))).thenReturn(page);
        when(galleryPhotoRepository.findByFolderIdOrderByUploadedAtDesc(1L)).thenReturn(List.of());

        GalleryFolderPageResponse result = galleryFolderService.getAllWithPhotos(0, 10);

        assertThat(result.hasMore()).isFalse();
    }

    @Test
    @DisplayName("getAllWithPhotos requests the exact page and size given")
    void getAllWithPhotos_RequestsExactPageAndSize() {
        Page<GalleryFolder> page = new PageImpl<>(List.of());
        when(galleryFolderRepository.findAllByOrderByCreatedAtAsc(PageRequest.of(2, 5))).thenReturn(page);

        galleryFolderService.getAllWithPhotos(2, 5);

        verify(galleryFolderRepository).findAllByOrderByCreatedAtAsc(PageRequest.of(2, 5));
    }

    @Test
    @DisplayName("getAllWithPhotos places the cover photo first when the folder has one")
    void getAllWithPhotos_PlacesCoverPhotoFirst() {
        GalleryFolder folder = buildFolder(1L);
        GalleryPhoto recent = buildPhoto(10L, folder);
        GalleryPhoto cover = buildPhoto(11L, folder);
        folder.setCoverPhoto(cover);

        Page<GalleryFolder> page = new PageImpl<>(List.of(folder), PageRequest.of(0, 10), 1);

        when(galleryFolderRepository.findAllByOrderByCreatedAtAsc(any(Pageable.class))).thenReturn(page);
        when(galleryPhotoRepository.findByFolderIdOrderByUploadedAtDesc(1L)).thenReturn(List.of(recent, cover));

        GalleryFolderPageResponse result = galleryFolderService.getAllWithPhotos(0, 10);

        assertThat(result.folders().get(0).photos().get(0).id()).isEqualTo(11L);
    }
}
