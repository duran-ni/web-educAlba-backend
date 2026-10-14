package dev.duran.web_educAlba_backend.academy.gallery;

import jakarta.validation.constraints.NotBlank;

// Datos de entrada para crear una carpeta de galeria
public record GalleryFolderRequest(
    @NotBlank
    String name
) {

}
