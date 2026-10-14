package dev.duran.web_educAlba_backend.academy.gallery;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class GalleryPhotoService {

    private final GalleryPhotoRepository galleryPhotoRepository;
    private final GalleryFolderService galleryFolderService;
    private final Cloudinary cloudinary;

    public GalleryPhotoService(GalleryPhotoRepository galleryPhotoRepository,
            GalleryFolderService galleryFolderService, Cloudinary cloudinary) {
        this.galleryPhotoRepository = galleryPhotoRepository;
        this.galleryFolderService = galleryFolderService;
        this.cloudinary = cloudinary;
    }

    @Transactional(readOnly = true)
    public List<GalleryPhotoResponse> getAll() {
        return galleryPhotoRepository.findAllByOrderByUploadedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public GalleryPhotoResponse upload(MultipartFile image, String caption, Long folderId) {
        GalleryFolder folder = galleryFolderService.findFolderOrThrow(folderId);
        Map uploadResult = uploadToCloudinary(image);

        GalleryPhoto photo = GalleryPhoto.builder()
                .imageUrl((String) uploadResult.get("secure_url"))
                .publicId((String) uploadResult.get("public_id"))
                .caption(caption)
                .uploadedAt(LocalDateTime.now())
                .folder(folder)
                .build();

        GalleryPhoto savedPhoto = galleryPhotoRepository.save(photo);
        return toResponse(savedPhoto);
    }

    @Transactional
    public void delete(Long id) {
        GalleryPhoto photo = findPhotoOrThrow(id);
        deleteFromCloudinary(photo.getPublicId());
        galleryPhotoRepository.delete(photo);
    }

    private Map uploadToCloudinary(MultipartFile image) {
        try {
            return cloudinary.uploader().upload(image.getBytes(), ObjectUtils.emptyMap());
        } catch (IOException ex) {
            throw new GalleryUploadException("subir", ex);
        }
    }

    private void deleteFromCloudinary(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException ex) {
            throw new GalleryUploadException("eliminar", ex);
        }
    }

    private GalleryPhoto findPhotoOrThrow(Long id) {
        return galleryPhotoRepository.findById(id)
                .orElseThrow(() -> new GalleryPhotoNotFoundException(id));
    }

    private GalleryPhotoResponse toResponse(GalleryPhoto photo) {
        GalleryFolder folder = photo.getFolder();
        return new GalleryPhotoResponse(
                photo.getId(),
                photo.getImageUrl(),
                photo.getCaption(),
                photo.getUploadedAt(),
                folder != null ? folder.getId() : null,
                folder != null ? folder.getName() : null);
    }
}
