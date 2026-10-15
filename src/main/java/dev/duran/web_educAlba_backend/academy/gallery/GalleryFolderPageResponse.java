package dev.duran.web_educAlba_backend.academy.gallery;

import java.util.List;

// Una "pagina" de carpetas publicas: el contenido de esta pagina y si
// quedan mas carpetas por cargar, para que el frontend sepa cuando
// mostrar u ocultar el boton "Cargar Mas Recuerdos"
public record GalleryFolderPageResponse(
    List<GalleryFolderWithPhotosResponse> folders,
    boolean hasMore
) {

}
