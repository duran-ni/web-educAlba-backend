package dev.duran.web_educAlba_backend.academy;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class WorkshopNotFoundException extends RuntimeException {
    public WorkshopNotFoundException(Long id) {
        super("No existe ningun taller con id " + id);
    }
}
