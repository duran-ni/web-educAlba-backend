package dev.duran.web_educAlba_backend.academy.gallery;

import java.util.List;

// Datos de salida publicos: una carpeta con sus fotos ya incluidas,
// para que el frontend pinte cada seccion con una sola peticion
public record GalleryFolderWithPhotosResponse(
    Long id,
    String name,
    List<GalleryPhotoResponse> photos
) {

}
