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
                .mediaType(resolveMediaType(uploadResult))
                .caption(caption)
                .uploadedAt(LocalDateTime.now())
                .folder(folder)
                .build();

        GalleryPhoto savedPhoto = galleryPhotoRepository.save(photo);
        return toResponse(savedPhoto);
    }

    @Transactional
    public GalleryPhotoResponse updateCaption(Long id, GalleryPhotoCaptionRequest request) {
        GalleryPhoto photo = findPhotoOrThrow(id);
        photo.setCaption(request.caption());
        GalleryPhoto savedPhoto = galleryPhotoRepository.save(photo);
        return toResponse(savedPhoto);
    }

    @Transactional
    public void delete(Long id) {
        GalleryPhoto photo = findPhotoOrThrow(id);
        galleryFolderService.clearCoverIfMatches(id);
        deleteFromCloudinary(photo.getPublicId(), photo.getMediaType());
        galleryPhotoRepository.delete(photo);
    }

    private Map uploadToCloudinary(MultipartFile image) {
        try {
            // "resource_type: auto" permite que Cloudinary detecte por si mismo
            // si el archivo subido es una imagen o un video, y lo almacene y
            // procese de la forma adecuada para cada caso
            return cloudinary.uploader().upload(image.getBytes(), ObjectUtils.asMap("resource_type", "auto"));
        } catch (IOException ex) {
            throw new GalleryUploadException("subir", ex);
        }
    }

    private void deleteFromCloudinary(String publicId, GalleryMediaType mediaType) {
        try {
            // Cloudinary asume "image" por defecto en destroy() si no se le indica
            // lo contrario, por lo que hay que decirle explicitamente "video"
            // cuando corresponda, o no encontrara el archivo a borrar
            String resourceType = mediaType == GalleryMediaType.VIDEO ? "video" : "image";
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
        } catch (IOException ex) {
            throw new GalleryUploadException("eliminar", ex);
        }
    }

    // Traduce el "resource_type" que devuelve Cloudinary ("image" o "video")
    // a nuestro propio enum, para no acoplar el resto de la aplicacion al
    // formato de respuesta concreto de Cloudinary
    private GalleryMediaType resolveMediaType(Map uploadResult) {
        return "video".equals(uploadResult.get("resource_type"))
                ? GalleryMediaType.VIDEO
                : GalleryMediaType.PHOTO;
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
                photo.getMediaType(),
                photo.getCaption(),
                photo.getUploadedAt(),
                folder != null ? folder.getId() : null,
                folder != null ? folder.getName() : null);
    }
}
