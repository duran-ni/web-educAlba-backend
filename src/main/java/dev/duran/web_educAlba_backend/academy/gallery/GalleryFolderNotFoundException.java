package dev.duran.web_educAlba_backend.academy.gallery;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GalleryFolderNotFoundException extends RuntimeException {
    public GalleryFolderNotFoundException(Long id) {
        super("No existe ninguna carpeta de galeria con id " + id);
    }
}
