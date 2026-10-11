package dev.duran.web_educAlba_backend.academy.student;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class StudentHasEnrollmentsException extends RuntimeException {
    public StudentHasEnrollmentsException(Long id) {
        super("No se puede eliminar el alumno con id " + id + " porque tiene inscripciones a talleres");
    }
}
