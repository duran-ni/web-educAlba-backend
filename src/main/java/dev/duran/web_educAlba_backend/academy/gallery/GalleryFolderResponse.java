package dev.duran.web_educAlba_backend.academy.gallery;

import java.time.LocalDateTime;

// Datos de salida al consultar una carpeta desde el panel de administracion
public record GalleryFolderResponse(
    Long id,
    String name,
    LocalDateTime createdAt,
    long photoCount,
    Long coverPhotoId
) {

}
