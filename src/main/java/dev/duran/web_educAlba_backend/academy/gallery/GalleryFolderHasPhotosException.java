package dev.duran.web_educAlba_backend.academy.gallery;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class GalleryFolderHasPhotosException extends RuntimeException {
    public GalleryFolderHasPhotosException(Long id) {
        super("No se puede eliminar la carpeta con id " + id + " porque todavia tiene fotos");
    }
}
