package dev.duran.web_educAlba_backend.academy.gallery;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class GalleryPhotoNotInFolderException extends RuntimeException {
    public GalleryPhotoNotInFolderException(Long photoId, Long folderId) {
        super("La foto con id " + photoId + " no pertenece a la carpeta con id " + folderId);
    }
}
