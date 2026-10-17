package dev.duran.web_educAlba_backend.academy.teacher;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class TeacherUploadException extends RuntimeException {
    public TeacherUploadException(String action, Throwable cause) {
        super("No se ha podido " + action + " la foto en Cloudinary", cause);
    }
}
