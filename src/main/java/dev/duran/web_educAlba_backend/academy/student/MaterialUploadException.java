package dev.duran.web_educAlba_backend.academy.student;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class MaterialUploadException extends RuntimeException {
    public MaterialUploadException(String action, Throwable cause) {
        super("No se ha podido " + action + " el PDF en Cloudinary", cause);
    }
}
