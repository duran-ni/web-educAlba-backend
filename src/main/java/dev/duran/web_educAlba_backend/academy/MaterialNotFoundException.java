package dev.duran.web_educAlba_backend.academy;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MaterialNotFoundException extends RuntimeException {
    public MaterialNotFoundException(Long id) {
        super("No existe ningun material con id " + id);
    }
}
