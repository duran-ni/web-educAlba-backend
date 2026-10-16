package dev.duran.web_educAlba_backend.academy.gallery;

import java.time.LocalDateTime;

// Datos de salida al consultar una foto (o video) de la galeria.
// No se expone publicId: es un dato interno, solo lo necesita el backend
// para comunicarse con Cloudinary
public record GalleryPhotoResponse(
    Long id,
    String imageUrl,
    GalleryMediaType mediaType,
    String caption,
    LocalDateTime uploadedAt,
    Long folderId,
    String folderName
) {

}
