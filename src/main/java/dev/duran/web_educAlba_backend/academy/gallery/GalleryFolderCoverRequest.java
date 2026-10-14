package dev.duran.web_educAlba_backend.academy.gallery;

import jakarta.validation.constraints.NotNull;

// Datos de entrada para marcar una foto como portada de una carpeta
public record GalleryFolderCoverRequest(
    @NotNull
    Long photoId
) {

}
