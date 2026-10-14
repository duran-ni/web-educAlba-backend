package dev.duran.web_educAlba_backend.academy.gallery;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
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
    public List<GalleryFolderWithPhotosResponse> getAllWithPhotos() {
        return galleryFolderRepository.findAllByOrderByCreatedAtAsc().stream()
                .map(folder -> new GalleryFolderWithPhotosResponse(
                        folder.getId(),
                        folder.getName(),
                        galleryPhotoRepository.findByFolderIdOrderByUploadedAtDesc(folder.getId()).stream()
                                .map(this::toPhotoResponse)
                                .toList()))
                .toList();
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
    public void delete(Long id) {
        GalleryFolder folder = findFolderOrThrow(id);
        try {
            galleryFolderRepository.delete(folder);
            galleryFolderRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new GalleryFolderHasPhotosException(id);
        }
    }

    GalleryFolder findFolderOrThrow(Long id) {
        return galleryFolderRepository.findById(id)
                .orElseThrow(() -> new GalleryFolderNotFoundException(id));
    }

    private GalleryFolderResponse toResponse(GalleryFolder folder) {
        return new GalleryFolderResponse(
                folder.getId(),
                folder.getName(),
                folder.getCreatedAt(),
                galleryPhotoRepository.countByFolderId(folder.getId()));
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
