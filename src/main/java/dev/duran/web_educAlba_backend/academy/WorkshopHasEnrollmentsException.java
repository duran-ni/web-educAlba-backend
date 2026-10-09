package dev.duran.web_educAlba_backend.academy;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class WorkshopHasEnrollmentsException extends RuntimeException {
    public WorkshopHasEnrollmentsException(Long id) {
        super("No se puede eliminar el taller con id " + id + " porque tiene alumnos inscritos");
    }
}
