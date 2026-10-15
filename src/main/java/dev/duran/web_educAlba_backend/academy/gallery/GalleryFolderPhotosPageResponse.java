package dev.duran.web_educAlba_backend.academy.gallery;

import java.util.List;

// Datos de salida publicos: una carpeta con una pagina de sus fotos, y si
// quedan mas fotos por cargar, para la subpagina de carpeta paginada
public record GalleryFolderPhotosPageResponse(
    Long id,
    String name,
    List<GalleryPhotoResponse> photos,
    boolean hasMore
) {

}
