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
                .map(this::toWithPhotosResponse)
                .toList();

        return new GalleryFolderPageResponse(folders, folderPage.hasNext());
    }

    @Transactional(readOnly = true)
    public GalleryFolderPhotosPageResponse getOneWithPhotos(Long id, int page, int size) {
        GalleryFolder folder = findFolderOrThrow(id);

        Page<GalleryPhoto> photoPage = galleryPhotoRepository
                .findByFolderIdOrderByUploadedAtDesc(id, PageRequest.of(page, size));

        List<GalleryPhoto> photos = placeCoverFirstOnFirstPage(
                new ArrayList<>(photoPage.getContent()), folder, page);

        List<GalleryPhotoResponse> photoResponses = photos.stream()
                .map(this::toPhotoResponse)
                .toList();

        return new GalleryFolderPhotosPageResponse(
                folder.getId(), folder.getName(), photoResponses, photoPage.hasNext());
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

    // En la primera pagina, adelanta la foto de portada (si existe) a la
    // primera posicion. Si esa foto no estaba ya entre las de esta pagina
    // (por ser mas antigua que el resto), se saca la ultima foto de la
    // pagina para dejarle sitio: como cada pagina se sigue calculando por
    // fecha en la base de datos (offset/limite, independiente de este
    // reordenado en memoria), esa foto desplazada no se pierde ni se
    // duplica, sera la primera foto de la pagina siguiente
    private List<GalleryPhoto> placeCoverFirstOnFirstPage(List<GalleryPhoto> photos, GalleryFolder folder,
            int page) {
        GalleryPhoto coverPhoto = folder.getCoverPhoto();

        if (page != 0 || coverPhoto == null) {
            return photos;
        }

        boolean coverWasAlreadyInPage = photos.remove(coverPhoto);
        if (!coverWasAlreadyInPage && !photos.isEmpty()) {
            photos.remove(photos.size() - 1);
        }

        photos.add(0, coverPhoto);
        return photos;
    }

    // Pone la foto marcada como portada al principio de la lista (si existe
    // alguna), dejando el resto en el orden habitual (mas reciente primero).
    // A diferencia de placeCoverFirstOnFirstPage, aqui se trabaja siempre con
    // la lista COMPLETA de fotos de la carpeta (sin paginar), por eso no hay
    // riesgo de perder ni duplicar fotos al adelantar la portada
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

    private GalleryFolderWithPhotosResponse toWithPhotosResponse(GalleryFolder folder) {
        return new GalleryFolderWithPhotosResponse(
                folder.getId(),
                folder.getName(),
                orderWithCoverFirst(folder).stream()
                        .map(this::toPhotoResponse)
                        .toList());
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
                photo.getMediaType(),
                photo.getCaption(),
                photo.getUploadedAt(),
                photo.getFolder().getId(),
                photo.getFolder().getName());
    }
}
