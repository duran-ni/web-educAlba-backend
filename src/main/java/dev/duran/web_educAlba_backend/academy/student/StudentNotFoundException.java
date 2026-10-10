package dev.duran.web_educAlba_backend.academy;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class StudentNotFoundException extends RuntimeException {
    public StudentNotFoundException(Long id) {
        super("No existe ningun alumno con id " + id);
    }
}
