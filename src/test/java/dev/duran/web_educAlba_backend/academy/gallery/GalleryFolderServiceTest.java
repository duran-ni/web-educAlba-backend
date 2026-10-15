package dev.duran.web_educAlba_backend.academy.gallery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @Test
    @DisplayName("getOneWithPhotos returns the folder with its paginated photos")
    void getOneWithPhotos_ReturnsFolderWithPhotos() {
        GalleryFolder folder = buildFolder(1L);
        GalleryPhoto photo = buildPhoto(10L, folder);
        Page<GalleryPhoto> photoPage = new PageImpl<>(List.of(photo), PageRequest.of(0, 8), 1);

        when(galleryFolderRepository.findById(1L)).thenReturn(Optional.of(folder));
        when(galleryPhotoRepository.findByFolderIdOrderByUploadedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(photoPage);

        GalleryFolderPhotosPageResponse result = galleryFolderService.getOneWithPhotos(1L, 0, 8);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.photos()).hasSize(1);
    }

    @Test
    @DisplayName("getOneWithPhotos throws when the folder does not exist")
    void getOneWithPhotos_ThrowsWhenFolderDoesNotExist() {
        when(galleryFolderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> galleryFolderService.getOneWithPhotos(1L, 0, 8))
                .isInstanceOf(GalleryFolderNotFoundException.class);
    }

    @Test
    @DisplayName("getOneWithPhotos returns hasMore true when the repository reports a next page")
    void getOneWithPhotos_ReturnsHasMoreTrue_WhenRepositoryHasNextPage() {
        GalleryFolder folder = buildFolder(1L);
        GalleryPhoto photo = buildPhoto(10L, folder);
        Page<GalleryPhoto> photoPage = new PageImpl<>(List.of(photo), PageRequest.of(0, 1), 2);

        when(galleryFolderRepository.findById(1L)).thenReturn(Optional.of(folder));
        when(galleryPhotoRepository.findByFolderIdOrderByUploadedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(photoPage);

        GalleryFolderPhotosPageResponse result = galleryFolderService.getOneWithPhotos(1L, 0, 1);

        assertThat(result.hasMore()).isTrue();
    }

    @Test
    @DisplayName("getOneWithPhotos returns hasMore false when the repository reports no next page")
    void getOneWithPhotos_ReturnsHasMoreFalse_WhenRepositoryHasNoNextPage() {
        GalleryFolder folder = buildFolder(1L);
        GalleryPhoto photo = buildPhoto(10L, folder);
        Page<GalleryPhoto> photoPage = new PageImpl<>(List.of(photo), PageRequest.of(0, 10), 1);

        when(galleryFolderRepository.findById(1L)).thenReturn(Optional.of(folder));
        when(galleryPhotoRepository.findByFolderIdOrderByUploadedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(photoPage);

        GalleryFolderPhotosPageResponse result = galleryFolderService.getOneWithPhotos(1L, 0, 10);

        assertThat(result.hasMore()).isFalse();
    }

    @Test
    @DisplayName("getOneWithPhotos requests the exact page and size given")
    void getOneWithPhotos_RequestsExactPageAndSize() {
        GalleryFolder folder = buildFolder(1L);
        Page<GalleryPhoto> photoPage = new PageImpl<>(List.of());

        when(galleryFolderRepository.findById(1L)).thenReturn(Optional.of(folder));
        when(galleryPhotoRepository.findByFolderIdOrderByUploadedAtDesc(1L, PageRequest.of(2, 5)))
                .thenReturn(photoPage);

        galleryFolderService.getOneWithPhotos(1L, 2, 5);

        verify(galleryPhotoRepository).findByFolderIdOrderByUploadedAtDesc(1L, PageRequest.of(2, 5));
    }

    @Test
    @DisplayName("getOneWithPhotos places the cover photo first on page one when it was already there")
    void getOneWithPhotos_PlacesCoverPhotoFirst_WhenAlreadyInPage() {
        GalleryFolder folder = buildFolder(1L);
        GalleryPhoto recent = buildPhoto(10L, folder);
        GalleryPhoto cover = buildPhoto(11L, folder);
        folder.setCoverPhoto(cover);

        Page<GalleryPhoto> photoPage = new PageImpl<>(List.of(recent, cover), PageRequest.of(0, 8), 2);
        when(galleryFolderRepository.findById(1L)).thenReturn(Optional.of(folder));
        when(galleryPhotoRepository.findByFolderIdOrderByUploadedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(photoPage);

        GalleryFolderPhotosPageResponse result = galleryFolderService.getOneWithPhotos(1L, 0, 8);

        assertThat(result.photos()).hasSize(2);
        assertThat(result.photos().get(0).id()).isEqualTo(11L);
    }

    @Test
    @DisplayName("getOneWithPhotos swaps the last photo out to fit an older cover photo first, "
            + "without losing or duplicating it")
    void getOneWithPhotos_PlacesCoverPhotoFirst_WhenOlderThanThePage() {
        GalleryFolder folder = buildFolder(1L);
        GalleryPhoto recent = buildPhoto(10L, folder);
        GalleryPhoto oldest = buildPhoto(11L, folder);
        GalleryPhoto olderCover = buildPhoto(99L, folder);
        folder.setCoverPhoto(olderCover);

        // La portada (99L) es mas antigua que las fotos de esta pagina, asi
        // que el repositorio no la devuelve en esta consulta
        Page<GalleryPhoto> photoPage = new PageImpl<>(List.of(recent, oldest), PageRequest.of(0, 2), 3);
        when(galleryFolderRepository.findById(1L)).thenReturn(Optional.of(folder));
        when(galleryPhotoRepository.findByFolderIdOrderByUploadedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(photoPage);

        GalleryFolderPhotosPageResponse result = galleryFolderService.getOneWithPhotos(1L, 0, 2);

        // El tamaño de pagina se mantiene (2), la portada queda primera y la
        // ultima foto natural de la pagina (11L) se saca, sin desaparecer:
        // el "hasMore" sigue viniendo del repositorio, asi que esa foto
        // seguira disponible al pedir la pagina siguiente
        assertThat(result.photos()).hasSize(2);
        assertThat(result.photos().get(0).id()).isEqualTo(99L);
        assertThat(result.photos().get(1).id()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getOneWithPhotos does not reorder the cover photo on pages after the first")
    void getOneWithPhotos_DoesNotReorderCoverPhoto_OnLaterPages() {
        GalleryFolder folder = buildFolder(1L);
        GalleryPhoto first = buildPhoto(20L, folder);
        GalleryPhoto second = buildPhoto(21L, folder);
        GalleryPhoto cover = buildPhoto(99L, folder);
        folder.setCoverPhoto(cover);

        Page<GalleryPhoto> photoPage = new PageImpl<>(List.of(first, second), PageRequest.of(1, 2), 4);
        when(galleryFolderRepository.findById(1L)).thenReturn(Optional.of(folder));
        when(galleryPhotoRepository.findByFolderIdOrderByUploadedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(photoPage);

        GalleryFolderPhotosPageResponse result = galleryFolderService.getOneWithPhotos(1L, 1, 2);

        assertThat(result.photos()).hasSize(2);
        assertThat(result.photos().get(0).id()).isEqualTo(20L);
        assertThat(result.photos().get(1).id()).isEqualTo(21L);
    }
}
