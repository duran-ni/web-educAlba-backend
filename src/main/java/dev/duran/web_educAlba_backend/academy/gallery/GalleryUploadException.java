package dev.duran.web_educAlba_backend.academy.gallery;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class GalleryUploadException extends RuntimeException {
    public GalleryUploadException(String action, Throwable cause) {
        super("No se ha podido " + action + " la imagen en Cloudinary", cause);
    }
}
