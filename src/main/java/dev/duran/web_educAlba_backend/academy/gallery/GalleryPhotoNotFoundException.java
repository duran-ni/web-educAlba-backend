package dev.duran.web_educAlba_backend.academy.gallery;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class GalleryPhotoNotFoundException extends RuntimeException {
    public GalleryPhotoNotFoundException(Long id) {
        super("No existe ninguna foto de galeria con id " + id);
    }
}
