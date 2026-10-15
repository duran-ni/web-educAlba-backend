package dev.duran.web_educAlba_backend.academy.gallery;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GalleryFolderService {

    private final GalleryFolderRepository galleryFolderRepository;
    private final GalleryPhotoRepository galleryPhotoRepository;

    public GalleryFolderService(GalleryFolderRepository galleryFolderRepository,
            GalleryPhotoRepository galleryPhotoRepository) {
        this.galleryFolderRepository = galleryFolderRepository;
        this.galleryPhotoRepository = galleryPhotoRepository;
    }

    @Transactional(readOnly = true)
    public List<GalleryFolderResponse> getAll() {
        return galleryFolderRepository.findAllByOrderByCreatedAtAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GalleryFolderPageResponse getAllWithPhotos(int page, int size) {
        Page<GalleryFolder> folderPage = galleryFolderRepository
                .findAllByOrderByCreatedAtAsc(PageRequest.of(page, size));

        List<GalleryFolderWithPhotosResponse> folders = folderPage.getContent().stream()
                .map(folder -> new GalleryFolderWithPhotosResponse(
                        folder.getId(),
                        folder.getName(),
                        orderWithCoverFirst(folder).stream()
                                .map(this::toPhotoResponse)
                                .toList()))
                .toList();

        return new GalleryFolderPageResponse(folders, folderPage.hasNext());
    }

    @Transactional
    public GalleryFolderResponse create(GalleryFolderRequest request) {
        GalleryFolder folder = GalleryFolder.builder()
                .name(request.name())
                .createdAt(LocalDateTime.now())
                .build();

        GalleryFolder savedFolder = galleryFolderRepository.save(folder);
        return toResponse(savedFolder);
    }

    @Transactional
    public GalleryFolderResponse setCoverPhoto(Long folderId, GalleryFolderCoverRequest request) {
        GalleryFolder folder = findFolderOrThrow(folderId);
        GalleryPhoto photo = galleryPhotoRepository.findById(request.photoId())
                .orElseThrow(() -> new GalleryPhotoNotFoundException(request.photoId()));

        if (!photo.getFolder().getId().equals(folderId)) {
            throw new GalleryPhotoNotInFolderException(photo.getId(), folderId);
        }

        folder.setCoverPhoto(photo);
        GalleryFolder savedFolder = galleryFolderRepository.save(folder);
        return toResponse(savedFolder);
    }

    @Transactional
    public void delete(Long id) {
        GalleryFolder folder = findFolderOrThrow(id);
        try {
            galleryFolderRepository.delete(folder);
            galleryFolderRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new GalleryFolderHasPhotosException(id);
        }
    }

    // Si la foto borrada era la portada de alguna carpeta, la desmarcamos antes
    // de que GalleryPhotoService la elimine, para no dejar una referencia rota
    @Transactional
    void clearCoverIfMatches(Long photoId) {
        galleryFolderRepository.findByCoverPhotoId(photoId)
                .ifPresent(folder -> {
                    folder.setCoverPhoto(null);
                    galleryFolderRepository.save(folder);
                });
    }

    GalleryFolder findFolderOrThrow(Long id) {
        return galleryFolderRepository.findById(id)
                .orElseThrow(() -> new GalleryFolderNotFoundException(id));
    }

    // Pone la foto marcada como portada al principio de la lista (si existe
    // alguna), dejando el resto en el orden habitual (mas reciente primero)
    private List<GalleryPhoto> orderWithCoverFirst(GalleryFolder folder) {
        List<GalleryPhoto> photos = galleryPhotoRepository.findByFolderIdOrderByUploadedAtDesc(folder.getId());
        GalleryPhoto coverPhoto = folder.getCoverPhoto();

        if (coverPhoto == null) {
            return photos;
        }

        List<GalleryPhoto> reordered = new ArrayList<>(photos);
        reordered.remove(coverPhoto);
        reordered.add(0, coverPhoto);
        return reordered;
    }

    private GalleryFolderResponse toResponse(GalleryFolder folder) {
        GalleryPhoto coverPhoto = folder.getCoverPhoto();
        return new GalleryFolderResponse(
                folder.getId(),
                folder.getName(),
                folder.getCreatedAt(),
                galleryPhotoRepository.countByFolderId(folder.getId()),
                coverPhoto != null ? coverPhoto.getId() : null);
    }

    private GalleryPhotoResponse toPhotoResponse(GalleryPhoto photo) {
        return new GalleryPhotoResponse(
                photo.getId(),
                photo.getImageUrl(),
                photo.getCaption(),
                photo.getUploadedAt(),
                photo.getFolder().getId(),
                photo.getFolder().getName());
    }
}
